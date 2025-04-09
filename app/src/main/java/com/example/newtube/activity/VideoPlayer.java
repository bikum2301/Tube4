package com.example.newtube.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View; // Import View
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem; // Import MediaItem
import androidx.media3.exoplayer.ExoPlayer; // Import ExoPlayer
import androidx.media3.ui.PlayerView; // Import PlayerView

import com.example.newtube.R;

public class VideoPlayer extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;

    private Uri videoUri; // Uri của video cần phát

    private boolean playWhenReady = true;
    private long playbackPosition = 0;
    private int currentWindow = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        playerView = findViewById(R.id.player_view);

        // Lấy Uri từ Intent đã gửi từ VideoFragment
        videoUri = getIntent().getData();

        if (videoUri == null) {
            // Xử lý lỗi nếu không có Uri
            Toast.makeText(this, "Lỗi: Không tìm thấy đường dẫn video", Toast.LENGTH_LONG).show();
            finish(); // Đóng activity nếu không có video
            return;
        }
    }

    // Khởi tạo Player (nên gọi trong onStart hoặc onResume)
    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            // Tạo MediaItem từ Uri
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);

            // Khôi phục trạng thái
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            player.prepare(); // Chuẩn bị player để phát
        }
    }

    // Giải phóng Player (nên gọi trong onStop hoặc onDestroy)
    private void releasePlayer() {
        if (player != null) {
            // Lưu lại trạng thái trước khi giải phóng
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentMediaItemIndex();

            player.release(); // Giải phóng tài nguyên
            player = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Khởi tạo player ở onStart để sẵn sàng khi activity hiển thị
        // (Trên API 24+, onStart đảm bảo activity đã hiển thị một phần)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi(); // Ẩn thanh trạng thái và điều hướng để xem toàn màn hình
        // Khởi tạo player ở onResume cho API < 24
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Giải phóng player ở onPause cho API < 24 để tiết kiệm tài nguyên nhanh chóng
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Giải phóng player ở onStop cho API 24+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }


    // Hàm ẩn System UI để có trải nghiệm toàn màn hình
    private void hideSystemUi() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            // Sử dụng WindowInsetsController (API 30+)
            // Code ẩn UI sẽ phức tạp hơn và cần xử lý lại khi controls hiện/ẩn
        } else {
            // Cách cũ hơn (API < 30)
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}