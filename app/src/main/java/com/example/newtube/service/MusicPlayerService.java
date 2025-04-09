package com.example.newtube.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle; // Đảm bảo import đúng MediaStyle
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.newtube.R;
import com.example.newtube.activity.MainActivity;
import com.example.newtube.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Thêm import cho Objects.equals

public class MusicPlayerService extends Service {

    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private static final int NOTIFICATION_ID = 1;

    // Actions cho Intent
    public static final String ACTION_SET_PLAYLIST_AND_PLAY = "com.example.newtube.ACTION_SET_PLAYLIST_AND_PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.example.newtube.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.example.newtube.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.example.newtube.ACTION_PREVIOUS";
    public static final String ACTION_STOP_SERVICE = "com.example.newtube.ACTION_STOP_SERVICE";

    // Extras cho Intent
    public static final String EXTRA_SONG_LIST = "EXTRA_SONG_LIST";
    public static final String EXTRA_START_POSITION = "EXTRA_START_POSITION";

    private ExoPlayer player;
    private List<Song> currentPlaylist = new ArrayList<>();
    private int currentSongIndex = -1;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        initializePlayer();
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            player.addListener(playerListener);
            Log.d(TAG, "ExoPlayer initialized");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand received action: " + (intent != null ? intent.getAction() : "null intent"));
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SET_PLAYLIST_AND_PLAY:
                    handleSetPlaylistAndPlay(intent); // Gọi hàm đã được cập nhật
                    break;
                case ACTION_PLAY_PAUSE:
                    togglePlayPause();
                    break;
                case ACTION_NEXT:
                    playNextSong();
                    break;
                case ACTION_PREVIOUS:
                    playPreviousSong();
                    break;
                case ACTION_STOP_SERVICE:
                    stopService();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    // --- *** HÀM ĐÃ ĐƯỢC CẬP NHẬT VỚI LOGIC SO SÁNH PLAYLIST *** ---
    private void handleSetPlaylistAndPlay(Intent intent) {
        ArrayList<Song> incomingPlaylist = intent.getParcelableArrayListExtra(EXTRA_SONG_LIST);
        int incomingStartPosition = intent.getIntExtra(EXTRA_START_POSITION, 0);

        if (incomingPlaylist == null || incomingPlaylist.isEmpty()) {
            Log.e(TAG, "Received null or empty playlist in handleSetPlaylistAndPlay");
            stopService();
            return;
        }

        boolean isSamePlaylist = comparePlaylists(currentPlaylist, incomingPlaylist);
        int validStartPosition = (incomingStartPosition >= 0 && incomingStartPosition < incomingPlaylist.size()) ? incomingStartPosition : 0;

        if (player != null && isSamePlaylist && currentSongIndex != -1) {
            // Playlist giống nhau -> Chỉ cần seek
            Log.d(TAG, "Playlist is the same, seeking to position: " + validStartPosition);
            if (player.getCurrentMediaItemIndex() != validStartPosition) {
                player.seekTo(validStartPosition, 0);
                // Listener onMediaItemTransition sẽ cập nhật currentSongIndex và notification
            } else {
                // Nếu nhấn vào bài đang phát thì toggle play/pause (tùy chọn)
                // togglePlayPause();
                // Hoặc không làm gì cả nếu đã đúng bài
                Log.d(TAG, "Already at the correct song index.");
            }
            // Đảm bảo player chạy nếu đang pause
            if (!player.isPlaying()) {
                player.play();
            }
            // Không cần cập nhật index và notification ở đây nữa vì Listener sẽ làm

        } else {
            // Playlist mới hoặc player chưa sẵn sàng -> Reset hoàn chỉnh
            Log.d(TAG, "New playlist or player not ready/different. Resetting playlist.");
            this.currentPlaylist = new ArrayList<>(incomingPlaylist);
            this.currentSongIndex = validStartPosition; // Cập nhật index ngay lập tức

            if (player == null) {
                initializePlayer();
            } else {
                player.stop(); // Dừng hẳn và reset
                player.clearMediaItems();
            }

            List<MediaItem> mediaItems = new ArrayList<>();
            for (Song song : currentPlaylist) {
                if (song.getDataPath() != null) {
                    try {
                        mediaItems.add(MediaItem.fromUri(Uri.parse(song.getDataPath())));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing URI: " + song.getDataPath(), e);
                    }
                } else {
                    Log.w(TAG, "Song has null dataPath: " + song.getTitle());
                }
            }

            if (mediaItems.isEmpty()) {
                Log.e(TAG, "No valid media items to play after processing playlist");
                Toast.makeText(this, "Không có bài hát hợp lệ", Toast.LENGTH_SHORT).show();
                stopService();
                return;
            }

            // Quan trọng: Đặt index trước khi setMediaItems nếu bạn muốn bắt đầu từ vị trí cụ thể
            player.setMediaItems(mediaItems, this.currentSongIndex, 0);
            player.prepare();
            player.play();

            // Cần gọi startForeground ngay
            if (currentSongIndex != -1) {
                // Tạo notification ban đầu, ảnh sẽ load sau
                startForeground(NOTIFICATION_ID, createNotification(currentPlaylist.get(currentSongIndex), null));
            } else {
                Log.e(TAG,"Cannot start foreground, invalid index after setting playlist");
                stopService();
            }
        }
    }

    // --- *** HÀM SO SÁNH PLAYLIST ĐƠN GIẢN (ĐÃ THÊM) *** ---
    private boolean comparePlaylists(List<Song> list1, List<Song> list2) {
        if (list1 == null || list2 == null || list1.size() != list2.size() || list1.isEmpty()) {
            return false;
        }
        // So sánh ID của bài đầu và cuối (có thể chưa đủ chính xác hoàn toàn)
        // Cách tốt hơn là so sánh ID của tất cả bài hát nếu cần độ chính xác cao
        // Hoặc tạo playlist ID duy nhất gửi từ Fragment
        return list1.get(0).getId() == list2.get(0).getId() &&
                list1.get(list1.size() - 1).getId() == list2.get(list1.size() - 1).getId();
    }


    private void togglePlayPause() {
        if (player != null) {
            if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekToDefaultPosition(player.getCurrentMediaItemIndex());
                player.prepare();
                player.play();
            } else if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
            Log.d(TAG, "togglePlayPause: isPlaying = " + player.isPlaying());
            // Listener onIsPlayingChanged sẽ gọi updateNotification()
        }
    }

    private void playNextSong() {
        if (player != null && player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
            Log.d(TAG, "playNextSong");
            // Listener onMediaItemTransition sẽ gọi updateNotification()
        } else {
            Log.d(TAG, "playNextSong: No next item or player is null");
            // Optional: Stop or repeat
        }
    }

    private void playPreviousSong() {
        if (player != null) {
            if (player.getCurrentPosition() > 3000 || !player.hasPreviousMediaItem()) {
                player.seekTo(0);
                Log.d(TAG, "playPreviousSong: Seeking to 0");
            } else {
                player.seekToPreviousMediaItem();
                Log.d(TAG, "playPreviousSong: Seeking to previous item");
            }
            // Listener onMediaItemTransition sẽ gọi updateNotification() nếu index thay đổi
        } else {
            Log.d(TAG, "playPreviousSong: Player is null");
        }
    }

    private void stopService() {
        Log.d(TAG, "Stopping service via stopService()...");
        releasePlayer();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        if (player != null) {
            Log.d(TAG, "Releasing player in releasePlayer()");
            player.removeListener(playerListener);
            player.release();
            player = null;
            currentPlaylist.clear();
            currentSongIndex = -1;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // --- Player Listener ---
    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.d(TAG, "Listener onPlaybackStateChanged: " + playbackState);
            if (playbackState == Player.STATE_ENDED && player.getRepeatMode() != Player.REPEAT_MODE_ONE) { // Chỉ next nếu không repeat 1 bài
                playNextSong();
            }
            // Chỉ cập nhật noti khi state là READY hoặc BUFFERING để tránh spam khi IDLE/ENDED
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                updateNotification();
            }
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            // Chỉ cập nhật nếu index thực sự thay đổi và hợp lệ
            int newIndex = player.getCurrentMediaItemIndex();
            if (newIndex != currentSongIndex && newIndex >= 0 && newIndex < currentPlaylist.size()) {
                currentSongIndex = newIndex;
                Log.d(TAG, "Listener onMediaItemTransition: New index = " + currentSongIndex + ", Reason: " + reason);
                updateNotification();
            } else if (newIndex == currentSongIndex) {
                // Đôi khi transition xảy ra mà index không đổi (ví dụ khi seek trong cùng bài)
                // Không cần update noti trong trường hợp này nếu thông tin không đổi
            } else {
                Log.w(TAG,"onMediaItemTransition: Invalid new index " + newIndex);
            }
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            Log.e(TAG, "Listener onPlayerError: ", error);
            Toast.makeText(MusicPlayerService.this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
            playNextSong(); // Thử bỏ qua bài lỗi
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Log.d(TAG, "Listener onIsPlayingChanged: " + isPlaying);
            updateNotification(); // Cập nhật icon play/pause và trạng thái ongoing
        }
    };

    // --- Notification Handling ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID, "Trình phát nhạc", NotificationManager.IMPORTANCE_LOW);
                serviceChannel.setDescription("Điều khiển phát nhạc");
                serviceChannel.setSound(null, null);
                serviceChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.d(TAG, "Notification channel already exists");
            }
        }
    }

    private void updateNotification() {
        // Kiểm tra xem có thể tạo notification không
        if (player == null || currentSongIndex < 0 || currentSongIndex >= currentPlaylist.size()) {
            Log.w(TAG,"Cannot update notification: player is null or index is invalid ("+ currentSongIndex + ")");
            // Có thể cân nhắc dừng foreground nếu player không hợp lệ
            // stopForeground(true);
            return;
        }

        Song song = currentPlaylist.get(currentSongIndex);
        // Lấy ảnh bìa đã load trước đó nếu có, không thì load lại
        // (Cách này không hiệu quả lắm, nên dùng loadAlbumArtAndUpdateNotification)
        // Bitmap currentArt = ???; // Cần cơ chế lưu ảnh tốt hơn
        // Notification notification = createNotification(song, currentArt);

        // Tạo notification với ảnh null, sau đó load ảnh
        Notification notification = createNotification(song, null);
        notificationManager.notify(NOTIFICATION_ID, notification);
        loadAlbumArtAndUpdateNotification(song.getAlbumArtUri(), notification);
        Log.d(TAG,"Notification updated for index: " + currentSongIndex);
    }

    private Notification createNotification(Song currentSong, @Nullable Bitmap albumArtBitmap) {
        if (currentSong == null) {
            Log.e(TAG, "Cannot create notification, currentSong is null");
            // Trả về một notification cơ bản hoặc xử lý khác
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Lỗi")
                    .setContentText("Không thể tải thông tin bài hát")
                    .setSmallIcon(R.drawable.ic_music_note_24)
                    .build();
        }
        Log.d(TAG, "Building notification for: " + currentSong.getTitle());

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent prevPendingIntent = createActionIntent(ACTION_PREVIOUS, 1);
        PendingIntent playPausePendingIntent = createActionIntent(ACTION_PLAY_PAUSE, 2);
        PendingIntent nextPendingIntent = createActionIntent(ACTION_NEXT, 3);
        PendingIntent stopPendingIntent = createActionIntent(ACTION_STOP_SERVICE, 4);

        boolean isPlaying = player != null && player.isPlaying();
        int playPauseIcon = isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24;
        String playPauseTitle = isPlaying ? "Tạm dừng" : "Phát";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setSmallIcon(R.drawable.ic_music_note_24)
                .setLargeIcon(albumArtBitmap)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .setOngoing(isPlaying)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder.addAction(R.drawable.ic_skip_previous_24, "Trước", prevPendingIntent);
        builder.addAction(playPauseIcon, playPauseTitle, playPausePendingIntent);
        builder.addAction(R.drawable.ic_skip_next_24, "Sau", nextPendingIntent);

        // Dùng đúng lớp MediaStyle từ androidx.media
        MediaStyle mediaStyle = new MediaStyle()
                // .setMediaSession(...) // Chưa tích hợp
                .setShowActionsInCompactView(0, 1, 2); // Index của Previous, Play/Pause, Next

        builder.setStyle(mediaStyle);

        return builder.build();
    }

    private PendingIntent createActionIntent(String action, int requestCode) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void loadAlbumArtAndUpdateNotification(String uriString, Notification baseNotification) {
        if (player == null || currentSongIndex < 0 || currentSongIndex >= currentPlaylist.size()) {
            // Không load ảnh nếu trạng thái không hợp lệ
            return;
        }
        // Lấy lại bài hát hiện tại phòng trường hợp index đã thay đổi trong lúc chờ
        Song currentValidSong = currentPlaylist.get(currentSongIndex);

        if (uriString == null) {
            Log.d(TAG, "Album art URI is null, skipping load for " + currentValidSong.getTitle());
            if (baseNotification.largeIcon != null) {
                Notification updatedNotification = createNotification(currentValidSong, null);
                notificationManager.notify(NOTIFICATION_ID, updatedNotification);
            }
            return;
        }

        Log.d(TAG, "Loading album art from: " + uriString + " for notification update (" + currentValidSong.getTitle() + ")");
        Glide.with(this)
                .asBitmap()
                .load(Uri.parse(uriString))
                .error(R.drawable.ic_album_24)
                .override(256, 256) // Giới hạn kích thước ảnh để tránh OutOfMemory trên notification
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Kiểm tra lại xem bài hát hiện tại có còn là bài hát lúc bắt đầu load không
                        if (player != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()
                                && Objects.equals(currentPlaylist.get(currentSongIndex).getAlbumArtUri(), uriString)) { // So sánh Uri
                            Log.d(TAG, "Album art loaded successfully for notification (" + currentValidSong.getTitle() + ")");
                            Notification updatedNotification = createNotification(currentValidSong, resource);
                            notificationManager.notify(NOTIFICATION_ID, updatedNotification);
                        } else {
                            Log.w(TAG, "Album art loaded but song has changed or player stopped. URI: " + uriString);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        // Kiểm tra lại xem bài hát hiện tại có còn là bài hát lúc bắt đầu load không
                        if (player != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()
                                && Objects.equals(currentPlaylist.get(currentSongIndex).getAlbumArtUri(), uriString)) {
                            Log.w(TAG, "Failed to load album art for notification (" + currentValidSong.getTitle() + ")");
                            if (baseNotification.largeIcon != null) {
                                Notification updatedNotification = createNotification(currentValidSong, null);
                                notificationManager.notify(NOTIFICATION_ID, updatedNotification);
                            }
                        } else {
                            Log.w(TAG, "Failed to load album art, but song has changed or player stopped. URI: " + uriString);
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }
}