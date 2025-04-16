package com.example.newtube.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout; // Đã import
import android.widget.ImageButton; // Thêm import
import android.widget.ImageView;   // Thêm import
import android.widget.TextView;    // Thêm import
import android.widget.Toast;     // Thêm import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Thêm import
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide; // Thêm import
import com.bumptech.glide.request.target.CustomTarget; // Thêm import
import com.bumptech.glide.request.transition.Transition; // Thêm import
import com.example.newtube.R;
import com.example.newtube.fragment.HomeFragment;
import com.example.newtube.fragment.LibraryFragment;
import com.example.newtube.fragment.music.MusicFragment; // Sửa import nếu cần
import com.example.newtube.fragment.VideoFragment;
import com.example.newtube.model.Song; // Thêm import
import com.example.newtube.service.MusicPlayerService; // Thêm import
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List; // Thêm import

// Implement PlayerCallback
public class MainActivity extends AppCompatActivity implements MusicPlayerService.PlayerCallback {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;

    // --- Views cho Mini Player ---
    private FrameLayout miniPlayerContainer;
    private ImageView ivMiniPlayerAlbumArt;
    private TextView tvMiniPlayerTitle;
    private TextView tvMiniPlayerArtist;
    private ImageButton btnMiniPlayerPlayPause;

    // --- Biến cho Service Binding ---
    private MusicPlayerService musicService;
    private boolean isServiceBound = false;

    // --- ServiceConnection ---
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            musicService.setPlayerCallback(MainActivity.this); // Đăng ký callback
            // Cập nhật UI ngay khi kết nối thành công với trạng thái hiện tại của service

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service Disconnected");
            isServiceBound = false;
            musicService = null; // Quan trọng: đặt service về null
            updateMiniPlayerUI(null, false); // Ẩn mini player
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        // Ánh xạ Views của Mini Player
        miniPlayerContainer = findViewById(R.id.mini_player_container);
        ivMiniPlayerAlbumArt = findViewById(R.id.iv_mini_player_album_art);
        tvMiniPlayerTitle = findViewById(R.id.tv_mini_player_title);
        tvMiniPlayerArtist = findViewById(R.id.tv_mini_player_artist);
        btnMiniPlayerPlayPause = findViewById(R.id.btn_mini_player_play_pause);

        // --- Xử lý sự kiện Click cho Mini Player ---
        miniPlayerContainer.setOnClickListener(v -> {
            Log.d(TAG,"Mini player clicked");
            // Mở Full Screen Player Activity
            Intent intent = new Intent(this, FullScreenPlayerActivity.class);
            // Không cần truyền dữ liệu gì vì FullScreenPlayerActivity sẽ tự bind và lấy trạng thái từ service
            startActivity(intent);
        });

        btnMiniPlayerPlayPause.setOnClickListener(v -> {
            // Gửi action đến Service để Play/Pause
            if (isServiceBound) { // Chỉ gửi khi đã bind
                Intent intent = new Intent(this, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_PLAY_PAUSE);
                ContextCompat.startForegroundService(this, intent); // Vẫn dùng startForegroundService
                Log.d(TAG,"Sent ACTION_PLAY_PAUSE");
            } else {
                Log.w(TAG, "Play/Pause clicked but service not bound");
            }
        });

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) selectedFragment = new HomeFragment();
            else if (itemId == R.id.nav_music) selectedFragment = new MusicFragment();
            else if (itemId == R.id.nav_video) selectedFragment = new VideoFragment();
            else if (itemId == R.id.nav_library) selectedFragment = new LibraryFragment();

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Mini Player sẽ được hiển thị/ẩn trong updateMiniPlayerUI
        // miniPlayerContainer.setVisibility(View.GONE); // Không cần set ở đây nữa
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind với Service
        Intent intent = new Intent(this, MusicPlayerService.class);
        // bindService sẽ tự start service nếu nó chưa chạy (nhờ flag BIND_AUTO_CREATE)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding service...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind khỏi Service
        if (isServiceBound) {
            musicService.removePlayerCallback(); // Hủy đăng ký callback
            unbindService(serviceConnection);
            isServiceBound = false;
            Log.d(TAG, "Unbinding service...");
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss(); // Dùng commitAllowingStateLoss nếu có thể gọi từ callback service
    }

    // --- Triển khai các phương thức từ PlayerCallback ---

    @Override
    public void onPlaylistChanged(List<Song> playlist, int currentSongIndex) {
        Log.d(TAG, "Callback: onPlaylistChanged");
        if (!playlist.isEmpty() && currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            updateMiniPlayerUI(playlist.get(currentSongIndex), isServiceBound && musicService.isPlaying());
        } else {
            updateMiniPlayerUI(null, false);
        }
    }

    @Override
    public void onTrackChanged(Song newSong, int newIndex) {
        Log.d(TAG, "Callback: onTrackChanged - " + (newSong != null ? newSong.getTitle() : "null"));
        updateMiniPlayerUI(newSong, isServiceBound && musicService.isPlaying());
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        Log.d(TAG, "Callback: onStateChanged - isPlaying=" + isPlaying);
        // Chỉ cập nhật nút bấm, không cần gọi updateMiniPlayerUI hoàn chỉnh
        btnMiniPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24);
        // Hiển thị/ẩn miniplayer nếu cần
        if (isServiceBound && musicService != null && musicService.getCurrentPlayingSong() != null) {
            miniPlayerContainer.setVisibility(View.VISIBLE);
        } else {
            // Có thể thêm logic ẩn nếu muốn (ví dụ: khi danh sách rỗng)
        }
    }

    @Override
    public void onPlayerStopped() {
        Log.d(TAG, "Callback: onPlayerStopped");
        updateMiniPlayerUI(null, false); // Ẩn Mini Player
    }

    @Override
    public void onShuffleModeChanged(boolean enabled) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    // --- Hàm cập nhật UI Mini Player ---
    private void updateMiniPlayerUI(@Nullable Song song, boolean isPlaying) {
        if (song != null) {
            miniPlayerContainer.setVisibility(View.VISIBLE);
            tvMiniPlayerTitle.setText(song.getTitle());
            tvMiniPlayerArtist.setText(song.getArtist());
            btnMiniPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24);

            // Load ảnh bìa cho Mini Player
            // Sử dụng this (Context của Activity)
            Glide.with(this)
                    .asBitmap()
                    .load(song.getAlbumArtPath())
                    .error(R.drawable.ic_album_24)
                    .placeholder(R.drawable.ic_album_24)
                    .override(128, 128) // Ảnh nhỏ hơn cho mini player
                    .centerCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Kiểm tra lại service và bài hát trước khi set ảnh
                            if (isServiceBound && musicService != null) {
                                Song currentSongInService = musicService.getCurrentPlayingSong();
                                if (currentSongInService != null && currentSongInService.getId() == song.getId()) {
                                    ivMiniPlayerAlbumArt.setImageBitmap(resource);
                                }
                            }
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            ivMiniPlayerAlbumArt.setImageDrawable(placeholder);
                        }
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (isServiceBound && musicService != null) {
                                Song currentSongInService = musicService.getCurrentPlayingSong();
                                if (currentSongInService != null && currentSongInService.getId() == song.getId()) {
                                    ivMiniPlayerAlbumArt.setImageDrawable(errorDrawable);
                                }
                            }
                        }
                    });

        } else {
            miniPlayerContainer.setVisibility(View.GONE);
        }
    }
}