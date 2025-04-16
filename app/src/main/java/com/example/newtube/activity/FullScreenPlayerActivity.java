package com.example.newtube.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PorterDuff; // *** THÊM IMPORT ***
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.newtube.R;
import com.example.newtube.model.Song;
import com.example.newtube.service.MusicPlayerService;
import androidx.media3.common.Player;

import java.util.List;
import java.util.Locale;

public class FullScreenPlayerActivity extends AppCompatActivity implements MusicPlayerService.PlayerCallback {

    private static final String TAG = "FullScreenPlayer";

    // Views
    private Toolbar toolbar;
    private ImageView ivAlbumArt;
    private TextView tvTitle, tvArtist;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalDuration;
    private ImageButton btnPrevious, btnPlayPause, btnNext;
    private ImageButton btnShuffle, btnRepeat;

    // Service
    private MusicPlayerService musicService;
    private boolean isServiceBound = false;
    private Intent serviceIntent;

    // Handler & Runnable
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;
    private boolean isUserSeekingSeekBar = false; // Cờ kiểm soát seekbar

    // Biến trạng thái cục bộ (lấy từ service)
    private Song currentSong;
    private boolean isPlaying = false;
    private int repeatMode = Player.REPEAT_MODE_OFF;
    private boolean shuffleModeEnabled = false;

    // ServiceConnection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            musicService.setPlayerCallback(FullScreenPlayerActivity.this);
            // Không gọi notifyCurrentState ở đây nữa, nó được gọi trong setPlayerCallback
            startUpdatingProgress(); // Bắt đầu cập nhật progress khi kết nối
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service Disconnected");
            isServiceBound = false;
            musicService = null;
            stopUpdatingProgress();
            Toast.makeText(FullScreenPlayerActivity.this, "Mất kết nối dịch vụ nhạc", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_player);

        serviceIntent = new Intent(this, MusicPlayerService.class);

        // Ánh xạ Views
        // ... (ánh xạ giữ nguyên)
        toolbar = findViewById(R.id.toolbar_player);
        ivAlbumArt = findViewById(R.id.iv_player_album_art);
        tvTitle = findViewById(R.id.tv_player_title);
        tvArtist = findViewById(R.id.tv_player_artist);
        seekBar = findViewById(R.id.seekbar_player);
        tvCurrentTime = findViewById(R.id.tv_player_current_time);
        tvTotalDuration = findViewById(R.id.tv_player_total_duration);
        btnPrevious = findViewById(R.id.btn_player_previous);
        btnPlayPause = findViewById(R.id.btn_player_play_pause);
        btnNext = findViewById(R.id.btn_player_next);
        btnShuffle = findViewById(R.id.btn_player_shuffle);
        btnRepeat = findViewById(R.id.btn_player_repeat);

        // Setup Toolbar
        // ... (giữ nguyên)
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup Listeners
        btnPlayPause.setOnClickListener(v -> sendActionToService(MusicPlayerService.ACTION_PLAY_PAUSE));
        btnNext.setOnClickListener(v -> sendActionToService(MusicPlayerService.ACTION_NEXT));
        btnPrevious.setOnClickListener(v -> sendActionToService(MusicPlayerService.ACTION_PREVIOUS));

        // *** CẬP NHẬT LISTENER CHO SHUFFLE VÀ REPEAT ***
        btnShuffle.setOnClickListener(v -> sendActionToService(MusicPlayerService.ACTION_TOGGLE_SHUFFLE));
        btnRepeat.setOnClickListener(v -> sendActionToService(MusicPlayerService.ACTION_CYCLE_REPEAT));

        // Setup Listener cho SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userProgress = progress;
                    tvCurrentTime.setText(formatMillis(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeekingSeekBar = true;
                stopUpdatingProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeekingSeekBar = false;
                // *** GỬI LỆNH SEEK ĐẾN SERVICE ***
                if (isServiceBound) {
                    // Cách 1: Gọi phương thức trực tiếp (nếu có)
                    // musicService.seekTo(userProgress); // Cần thêm hàm này vào Service
                    // Cách 2: Gửi Action Intent (nhất quán hơn)
                    Intent seekIntent = new Intent(FullScreenPlayerActivity.this, MusicPlayerService.class);
                    seekIntent.setAction(MusicPlayerService.ACTION_SEEK_TO);
                    seekIntent.putExtra(MusicPlayerService.EXTRA_SEEK_POSITION, (long)userProgress); // Gửi kiểu long
                    ContextCompat.startForegroundService(FullScreenPlayerActivity.this, seekIntent);
                    Log.d(TAG, "Sent ACTION_SEEK_TO: " + userProgress);
                }
                startUpdatingProgress(); // Bắt đầu cập nhật lại
            }
        });

        // Khởi tạo Runnable
        updateProgressRunnable = () -> {
            updateSeekBarProgress();
            // Chỉ lặp lại nếu activity chưa bị hủy và service còn chạy
            if (isServiceBound && !isFinishing()) {
                progressHandler.postDelayed(updateProgressRunnable, 500); // Cập nhật nhanh hơn (0.5 giây)
            }
        };

        // Cập nhật UI ban đầu cho các nút shuffle/repeat (màu xám)
        updateShuffleButtonUI(false);
        updateRepeatButtonUI(Player.REPEAT_MODE_OFF);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding service...");
        // Nếu service đang chạy và có bài hát, nên bắt đầu cập nhật progress ngay
        if (isServiceBound && musicService != null && musicService.isPlaying()) {
            startUpdatingProgress();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            stopUpdatingProgress();
            musicService.removePlayerCallback();
            unbindService(serviceConnection);
            isServiceBound = false;
            Log.d(TAG, "Unbinding service...");
        }
    }

    private void sendActionToService(String action) {
        // ... (giữ nguyên)
        if (isServiceBound) {
            Intent intent = new Intent(this, MusicPlayerService.class);
            intent.setAction(action);
            ContextCompat.startForegroundService(this, intent);
            Log.d(TAG, "Sent action: " + action);
        } else { Log.w(TAG,"Cannot send action " + action + ", service not bound"); }
    }

    // Hàm cập nhật UI chính
    private void updateUI(Song song, boolean isPlaying) {
        this.currentSong = song;
        this.isPlaying = isPlaying;

        if (song != null) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            tvTitle.setSelected(true);
            btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24);

            Glide.with(this) /* ... (load ảnh như cũ) ... */
                    .asBitmap().load(song.getAlbumArtPath()).placeholder(R.drawable.ic_album_24).error(R.drawable.ic_album_24).into(ivAlbumArt);

            // Lấy duration từ service nếu có thể
            long duration = 0;
            if (isServiceBound && musicService != null) {
                duration = musicService.getPlayerDuration(); // *** SỬA: GỌI PHƯƠNG THỨC MỚI ***
            }
            if (duration <= 0 && song.getDuration() > 0) { // Fallback về model nếu service chưa sẵn sàng
                duration = song.getDuration();
            }

            if (duration > 0) {
                tvTotalDuration.setText(formatMillis(duration));
                seekBar.setMax((int) duration);
            } else {
                tvTotalDuration.setText("--:--");
                seekBar.setMax(100); // Đặt max tạm thời để tránh lỗi chia cho 0
            }
            // Không reset progress ở đây nữa, để updateSeekBarProgress xử lý
            // seekBar.setProgress(0);
            // tvCurrentTime.setText("00:00");

            // Cập nhật trạng thái shuffle/repeat ban đầu khi đổi bài
            if (isServiceBound && musicService != null) {
                updateShuffleButtonUI(musicService.getShuffleModeEnabled());
                updateRepeatButtonUI(musicService.getRepeatMode());
            }


        } else {
            finish(); // Đóng nếu không có bài hát
        }
    }

    // Hàm cập nhật tiến trình SeekBar
    private void startUpdatingProgress() {
        stopUpdatingProgress();
        progressHandler.post(updateProgressRunnable);
        // Log.d(TAG, "Started updating progress"); // Giảm log
    }

    private void stopUpdatingProgress() {
        progressHandler.removeCallbacks(updateProgressRunnable);
        // Log.d(TAG, "Stopped updating progress"); // Giảm log
    }

    private void updateSeekBarProgress() {
        if (isServiceBound && musicService != null && !isUserSeekingSeekBar) { // Chỉ cập nhật nếu người dùng không kéo
            // *** SỬA: GỌI PHƯƠNG THỨC MỚI ***
            long currentPosition = musicService.getPlayerCurrentPosition();
            long duration = musicService.getPlayerDuration();

            if (duration > 0) {
                if (seekBar.getMax() != (int) duration) {
                    seekBar.setMax((int) duration);
                    tvTotalDuration.setText(formatMillis(duration));
                }
                // Cập nhật progress mượt mà hơn
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    seekBar.setProgress((int) currentPosition, true);
                } else {
                    seekBar.setProgress((int) currentPosition);
                }
                tvCurrentTime.setText(formatMillis(currentPosition));
            } else {
                // Xử lý khi duration không hợp lệ
                seekBar.setProgress(0);
                tvCurrentTime.setText("00:00");
                // Có thể lấy duration từ model nếu service chưa sẵn sàng
                if (currentSong != null && currentSong.getDuration() > 0) {
                    if (seekBar.getMax() != (int) currentSong.getDuration()) {
                        seekBar.setMax((int) currentSong.getDuration());
                        tvTotalDuration.setText(formatMillis(currentSong.getDuration()));
                    }
                } else {
                    tvTotalDuration.setText("--:--");
                }
            }
        }
    }

    private String formatMillis(long millis) {
        // ... (giữ nguyên)
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        else return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }


    // --- Implement PlayerCallback ---

    @Override
    public void onPlaylistChanged(List<Song> playlist, int currentSongIndex) { /* ... như cũ ... */ }

    @Override
    public void onTrackChanged(Song newSong, int newIndex) {
        Log.d(TAG, "Callback: onTrackChanged");
        updateUI(newSong, this.isPlaying);
        // Không cần gọi startUpdatingProgress ở đây vì onStateChanged sẽ làm nếu isPlaying=true
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        Log.d(TAG, "Callback: onStateChanged - " + isPlaying);
        this.isPlaying = isPlaying;
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24);
        if (isPlaying) { startUpdatingProgress(); }
        else { stopUpdatingProgress(); }
    }

    @Override
    public void onPlayerStopped() {
        Log.d(TAG, "Callback: onPlayerStopped");
        stopUpdatingProgress();
        finish();
    }

    // *** THÊM IMPLEMENT CALLBACK CHO SHUFFLE/REPEAT ***
    @Override
    public void onShuffleModeChanged(boolean enabled) {
        Log.d(TAG, "Callback: onShuffleModeChanged - " + enabled);
        this.shuffleModeEnabled = enabled;
        updateShuffleButtonUI(enabled);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.d(TAG, "Callback: onRepeatModeChanged - " + repeatMode);
        this.repeatMode = repeatMode;
        updateRepeatButtonUI(repeatMode);
    }

    // --- *** THÊM HÀM CẬP NHẬT UI NÚT SHUFFLE/REPEAT *** ---
    private void updateShuffleButtonUI(boolean enabled) {
        if (enabled) {
            // Đổi màu nút Shuffle khi bật (ví dụ: dùng màu accent)
            int accentColor = ContextCompat.getColor(this, R.color.purple_500); // Thay bằng màu accent của bạn
            btnShuffle.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        } else {
            // Trả về màu mặc định (màu phụ của theme)
            int secondaryTextColor = ContextCompat.getColor(this, android.R.color.secondary_text_dark); // Hoặc lấy từ theme attribute
            // btnShuffle.clearColorFilter(); // Hoặc set màu cụ thể
            btnShuffle.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN); // Ví dụ set màu xám
        }
    }

    private void updateRepeatButtonUI(int repeatMode) {
        int accentColor = ContextCompat.getColor(this, R.color.purple_500); // Màu accent
        int secondaryTextColor = ContextCompat.getColor(this, android.R.color.secondary_text_dark); // Màu mặc định

        switch (repeatMode) {
            case Player.REPEAT_MODE_ONE:
                btnRepeat.setImageResource(R.drawable.ic_repeat_one_24); // Đổi icon thành repeat_one
                btnRepeat.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN); // Đổi màu thành accent
                break;
            case Player.REPEAT_MODE_ALL:
                btnRepeat.setImageResource(R.drawable.ic_repeat_24); // Icon repeat thường
                btnRepeat.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN); // Đổi màu thành accent
                break;
            case Player.REPEAT_MODE_OFF:
            default:
                btnRepeat.setImageResource(R.drawable.ic_repeat_24); // Icon repeat thường
                btnRepeat.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN); // Màu mặc định
                break;
        }
    }

    // Cần tạo icon ic_repeat_one_24.xml
}