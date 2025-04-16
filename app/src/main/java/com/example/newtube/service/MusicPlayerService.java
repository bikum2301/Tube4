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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle; // Import đúng MediaStyle
import androidx.media3.common.C; // Import cho TIME_UNSET
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.newtube.R;
import com.example.newtube.activity.MainActivity; // Dùng để mở lại app từ notification
import com.example.newtube.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public static final String ACTION_SEEK_TO = "com.example.newtube.ACTION_SEEK_TO";
    public static final String ACTION_TOGGLE_SHUFFLE = "com.example.newtube.ACTION_TOGGLE_SHUFFLE";
    public static final String ACTION_CYCLE_REPEAT = "com.example.newtube.ACTION_CYCLE_REPEAT";

    // Extras cho Intent
    public static final String EXTRA_SONG_LIST = "EXTRA_SONG_LIST";
    public static final String EXTRA_START_POSITION = "EXTRA_START_POSITION";
    public static final String EXTRA_SEEK_POSITION = "EXTRA_SEEK_POSITION";

    // Components
    private ExoPlayer player;
    private NotificationManager notificationManager;

    // State
    private List<Song> currentPlaylist = new ArrayList<>();
    private int currentSongIndex = -1;

    // Communication
    private final IBinder binder = new LocalBinder();
    private PlayerCallback playerCallback;

    // --- Binder và Callback ---
    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    public interface PlayerCallback {
        void onPlaylistChanged(List<Song> playlist, int currentSongIndex);
        void onTrackChanged(Song newSong, int newIndex);
        void onStateChanged(boolean isPlaying);
        void onPlayerStopped();
        void onShuffleModeChanged(boolean enabled);
        void onRepeatModeChanged(int repeatMode);
    }

    public void setPlayerCallback(PlayerCallback callback) {
        this.playerCallback = callback;
        Log.d(TAG, "PlayerCallback set");
        notifyCurrentState(); // Gửi trạng thái hiện tại ngay khi kết nối
    }

    public void removePlayerCallback() {
        this.playerCallback = null;
        Log.d(TAG, "PlayerCallback removed");
    }

    // Gửi trạng thái hiện tại đến Activity/Fragment đã đăng ký
    private void notifyCurrentState() {
        if (playerCallback == null) return;

        if (player != null && currentSongIndex != -1 && !currentPlaylist.isEmpty()) {
            Log.d(TAG, "notifyCurrentState: Sending current state...");
            playerCallback.onTrackChanged(currentPlaylist.get(currentSongIndex), currentSongIndex);
            playerCallback.onStateChanged(player.isPlaying());
            playerCallback.onShuffleModeChanged(player.getShuffleModeEnabled());
            playerCallback.onRepeatModeChanged(player.getRepeatMode());
        } else {
            Log.d(TAG, "notifyCurrentState: Sending player stopped state");
            playerCallback.onPlayerStopped();
        }
    }
    // --- Kết thúc Binder và Callback ---

    // --- Lifecycle Methods ---
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        initializePlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand action: " + (intent != null ? intent.getAction() : "null"));
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SET_PLAYLIST_AND_PLAY: handleSetPlaylistAndPlay(intent); break;
                case ACTION_PLAY_PAUSE: togglePlayPause(); break;
                case ACTION_NEXT: playNextSong(); break;
                case ACTION_PREVIOUS: playPreviousSong(); break;
                case ACTION_STOP_SERVICE: stopService(); break;
                case ACTION_SEEK_TO: seekTo(intent.getLongExtra(EXTRA_SEEK_POSITION, 0)); break;
                case ACTION_TOGGLE_SHUFFLE: toggleShuffleMode(); break;
                case ACTION_CYCLE_REPEAT: cycleRepeatMode(); break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        releasePlayer();
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { Log.d(TAG, "onBind"); return binder; }
    @Override public boolean onUnbind(Intent intent) { Log.d(TAG, "onUnbind"); return super.onUnbind(intent); }
    // --- Kết thúc Lifecycle Methods ---


    // --- Player Initialization and Release ---
    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            player.addListener(playerListener);
            Log.d(TAG, "ExoPlayer initialized");
        }
    }

    private void releasePlayer() {
        if (player != null) {
            Log.d(TAG, "Releasing player");
            player.removeListener(playerListener);
            player.release();
            player = null;
            currentPlaylist.clear();
            currentSongIndex = -1;
        }
    }
    // --- Kết thúc Player Initialization and Release ---

    private void updateAndStartForeground() {
        if (currentSongIndex != -1 && currentSongIndex < currentPlaylist.size()) {
            Song currentValidSong = currentPlaylist.get(currentSongIndex);
            if (currentValidSong != null) {
                Notification notification = createNotification(currentValidSong, null); // Tạo với ảnh null
                if (notification != null) { // Kiểm tra notification không null
                    try {
                        startForeground(NOTIFICATION_ID, notification);
                        Log.d(TAG, "Started foreground service.");
                        // Load ảnh sau khi đã start foreground
                        // *** SỬA Ở ĐÂY ***
                        loadAlbumArtAndUpdateNotification(currentValidSong.getAlbumArtPath(), notification);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting foreground service", e);
                        // Xử lý lỗi, có thể dừng service
                        stopService();
                    }
                } else {
                    Log.e(TAG, "Failed to create notification, stopping service.");
                    stopService();
                }
            } else {
                Log.e(TAG, "Current song at index " + currentSongIndex + " is null, stopping service.");
                stopService();
            }
        } else {
            Log.e(TAG, "Cannot start foreground, invalid index: " + currentSongIndex);
            stopService();
        }
    }
    // --- Action Handling Methods ---
    private void handleSetPlaylistAndPlay(Intent intent) {
        ArrayList<Song> incomingPlaylist = intent.getParcelableArrayListExtra(EXTRA_SONG_LIST);
        int incomingStartPosition = intent.getIntExtra(EXTRA_START_POSITION, 0);
        if (incomingPlaylist == null || incomingPlaylist.isEmpty()) { stopService(); return; }

        boolean isSamePlaylist = comparePlaylists(currentPlaylist, incomingPlaylist);
        int validStartPosition = (incomingStartPosition >= 0 && incomingStartPosition < incomingPlaylist.size()) ? incomingStartPosition : 0;

        if (player != null && isSamePlaylist && currentSongIndex != -1) {
            // Playlist giống nhau, chỉ seek
            Log.d(TAG, "Playlist same, seeking to: " + validStartPosition);
            if (player.getCurrentMediaItemIndex() != validStartPosition) {
                player.seekTo(validStartPosition, 0); // Listener sẽ xử lý cập nhật UI/callback
            }
            if (!player.isPlaying()) {
                player.play(); // Listener sẽ xử lý callback state
            }
        } else {
            // Playlist mới, reset hoàn toàn
            Log.d(TAG, "New playlist/reset needed.");
            this.currentPlaylist = new ArrayList<>(incomingPlaylist);
            this.currentSongIndex = validStartPosition;
            if (player == null) initializePlayer();
            else { player.stop(); player.clearMediaItems(); }

            List<MediaItem> mediaItems = new ArrayList<>();
            for (Song song : currentPlaylist) {
                // *** SỬA Ở ĐÂY ***
                if (song.getFilePath() != null) {
                    try {
                        // *** SỬA Ở ĐÂY ***
                        mediaItems.add(MediaItem.fromUri(Uri.parse(song.getFilePath())));
                    }
                    catch (Exception e) {
                        // *** SỬA Ở ĐÂY ***
                        Log.e(TAG, "URI parse error: " + song.getFilePath(), e);
                    }
                } else { Log.w(TAG, "Null filePath: " + song.getTitle()); } // Sửa log
            }
            if (mediaItems.isEmpty()) { stopService(); return; }

            player.setMediaItems(mediaItems, this.currentSongIndex, 0);
            player.prepare();
            player.play();

            // Gọi Callback ngay sau khi chuẩn bị xong
            if(playerCallback != null && currentSongIndex != -1){
                playerCallback.onPlaylistChanged(currentPlaylist, currentSongIndex);
                playerCallback.onTrackChanged(currentPlaylist.get(currentSongIndex), currentSongIndex);
                playerCallback.onStateChanged(true);
                playerCallback.onShuffleModeChanged(player.getShuffleModeEnabled());
                playerCallback.onRepeatModeChanged(player.getRepeatMode());
            }
            updateAndStartForeground(); // Hiển thị notification
        }
    }

    private boolean comparePlaylists(List<Song> list1, List<Song> list2) {
        if (list1 == null || list2 == null || list1.size() != list2.size() || list1.isEmpty()) return false;
        // So sánh đơn giản ID đầu và cuối
        // *** SỬA Ở ĐÂY (nếu ID là String) ***
        return Objects.equals(list1.get(0).getId(), list2.get(0).getId()) &&
                Objects.equals(list1.get(list1.size() - 1).getId(), list2.get(list1.size() - 1).getId());
    }

    private void togglePlayPause() {
        if (player != null) {
            if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekToDefaultPosition(player.getCurrentMediaItemIndex());
                player.prepare();
                player.play();
            } else if (player.isPlaying()) { player.pause(); }
            else { player.play(); }
            Log.d(TAG, "togglePlayPause: isPlaying = " + player.isPlaying());
        }
    }

    private void playNextSong() {
        if (player != null && player.hasNextMediaItem()) player.seekToNextMediaItem();
        else Log.d(TAG, "playNextSong: No next");
    }

    private void playPreviousSong() {
        if (player != null) {
            if (player.getCurrentPosition() > 3000 || !player.hasPreviousMediaItem()) player.seekTo(0);
            else player.seekToPreviousMediaItem();
        } else Log.d(TAG, "playPreviousSong: Player null");
    }

    private void seekTo(long positionMs) {
        if (player != null) { Log.d(TAG, "Seeking to: " + positionMs); player.seekTo(positionMs); }
    }

    private void toggleShuffleMode() {
        if (player != null) { player.setShuffleModeEnabled(!player.getShuffleModeEnabled()); Log.d(TAG, "Toggled shuffle"); }
    }

    private void cycleRepeatMode() {
        if (player != null) {
            int currentMode = player.getRepeatMode();
            int nextMode = (currentMode + 1) % 3; // Cycle through 0, 1, 2
            player.setRepeatMode(nextMode);
            Log.d(TAG, "Cycled repeat mode to: " + nextMode);
        }
    }

    private void stopService() {
        Log.d(TAG, "Stopping service...");
        if (playerCallback != null) playerCallback.onPlayerStopped();
        releasePlayer();
        stopForeground(true);
        stopSelf();
    }
    // --- Kết thúc Action Handling Methods ---


    // --- Player Listener ---
    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.d(TAG, "Listener onPlaybackStateChanged: " + playbackState);
            if (playbackState == Player.STATE_ENDED && player.getRepeatMode() != Player.REPEAT_MODE_ONE) {
                // Chỉ tự động next nếu không phải repeat one
                if (player.hasNextMediaItem()) {
                    playNextSong();
                } else {
                    // Hết playlist, dừng hoặc lặp lại tùy repeat mode
                    if (player.getRepeatMode() == Player.REPEAT_MODE_ALL) {
                        player.seekTo(0, 0); // Quay về đầu playlist
                        player.play();
                    } else {
                        // Dừng lại, có thể gọi stopService hoặc chỉ pause
                        player.pause();
                        player.seekTo(player.getCurrentMediaItemIndex(), 0); // Reset về đầu bài cuối
                        updateNotification(); // Cập nhật nút thành play
                    }
                }
            }
            // Cập nhật notification khi sẵn sàng hoặc đang buffer
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                updateNotification();
            }
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            if (player == null) return; // Tránh lỗi nếu player đã release
            int newIndex = player.getCurrentMediaItemIndex();
            // Chỉ xử lý nếu index thực sự thay đổi và hợp lệ
            if (newIndex != currentSongIndex && newIndex >= 0 && newIndex < currentPlaylist.size()) {
                currentSongIndex = newIndex;
                Log.d(TAG, "Listener onMediaItemTransition: New index = " + currentSongIndex);
                if (playerCallback != null) playerCallback.onTrackChanged(currentPlaylist.get(currentSongIndex), currentSongIndex);
                updateNotification(); // Cập nhật notification với bài hát mới
            } else if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                // Xử lý khi playlist thay đổi hoàn toàn (ví dụ sau setMediaItems)
                currentSongIndex = newIndex; // Cập nhật index mới
                if (playerCallback != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()) {
                    playerCallback.onTrackChanged(currentPlaylist.get(currentSongIndex), currentSongIndex);
                }
                updateNotification();
            }
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            Log.e(TAG, "Listener onPlayerError: ", error);
            Toast.makeText(MusicPlayerService.this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
            if (player != null && player.hasNextMediaItem()) {
                playNextSong(); // Thử bỏ qua bài lỗi nếu có bài tiếp theo
            } else {
                stopService(); // Dừng nếu là bài cuối hoặc lỗi nghiêm trọng
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Log.d(TAG, "Listener onIsPlayingChanged: " + isPlaying);
            if (playerCallback != null) playerCallback.onStateChanged(isPlaying);
            updateNotification(); // Cập nhật nút play/pause và trạng thái ongoing
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.d(TAG, "Listener onShuffleModeEnabledChanged: " + shuffleModeEnabled);
            if(playerCallback != null) playerCallback.onShuffleModeChanged(shuffleModeEnabled);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.d(TAG, "Listener onRepeatModeChanged: " + repeatMode);
            if(playerCallback != null) playerCallback.onRepeatModeChanged(repeatMode);
        }
    };
    // --- Kết thúc Player Listener ---


    // --- Notification Handling ---
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Nhạc đang phát", NotificationManager.IMPORTANCE_LOW);
                channel.setDescription("Điều khiển nhạc đang phát");
                channel.setSound(null, null); channel.setShowBadge(false);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Cập nhật và hiển thị notification
    private void updateNotification() {
        if (player == null || currentSongIndex < 0 || currentSongIndex >= currentPlaylist.size()) return;
        Song song = currentPlaylist.get(currentSongIndex);
        if (song == null) return;

        Notification notification = createNotification(song, null); // Tạo với ảnh null trước
        if (notification != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
            // *** SỬA Ở ĐÂY ***
            loadAlbumArtAndUpdateNotification(song.getAlbumArtPath(), notification); // Load ảnh sau
        } else {
            Log.e(TAG, "updateNotification: Failed to create notification");
        }
    }

    // Hàm tạo đối tượng Notification
    private Notification createNotification(Song currentSong, @Nullable Bitmap albumArtBitmap) {
        if (currentSong == null) {
            Log.e(TAG, "createNotification: currentSong is null");
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Lỗi").setContentText("Bài hát không hợp lệ").setSmallIcon(R.drawable.ic_music_note_24).build();
        }
        Log.d(TAG, "Building notification for: " + currentSong.getTitle());

        Intent mainIntent = new Intent(this, MainActivity.class); // Mở MainActivity
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent prevPendingIntent = createActionIntent(ACTION_PREVIOUS, 1);
        PendingIntent playPausePendingIntent = createActionIntent(ACTION_PLAY_PAUSE, 2);
        PendingIntent nextPendingIntent = createActionIntent(ACTION_NEXT, 3);
        PendingIntent stopPendingIntent = createActionIntent(ACTION_STOP_SERVICE, 4); // Cho nút đóng hoặc vuốt tắt

        boolean isPlaying = player != null && player.isPlaying();
        int playPauseIcon = isPlaying ? R.drawable.ic_pause_24 : R.drawable.ic_play_arrow_24;
        String playPauseTitle = isPlaying ? "Tạm dừng" : "Phát";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setSmallIcon(R.drawable.ic_music_note_24)
                .setLargeIcon(albumArtBitmap) // Sẽ được cập nhật sau nếu null
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(stopPendingIntent) // Khi vuốt tắt -> gọi stop service
                .setOngoing(isPlaying) // Chỉ không vuốt tắt được khi đang phát
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true); // Tránh rung/kêu mỗi lần cập nhật

        // Luôn thêm đủ 3 nút chính
        builder.addAction(R.drawable.ic_skip_previous_24, "Trước", prevPendingIntent);
        builder.addAction(playPauseIcon, playPauseTitle, playPausePendingIntent);
        builder.addAction(R.drawable.ic_skip_next_24, "Sau", nextPendingIntent);

        // Sử dụng MediaStyle
        MediaStyle mediaStyle = new MediaStyle()
                .setShowActionsInCompactView(0, 1, 2); // Hiển thị 3 nút chính khi thu gọn
        // .setMediaSession(...) // Có thể tích hợp MediaSession sau

        builder.setStyle(mediaStyle);

        return builder.build();
    }

    // Helper tạo PendingIntent cho các Action Button
    private PendingIntent createActionIntent(String action, int requestCode) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // Load ảnh bìa bất đồng bộ và cập nhật lại Notification
    private void loadAlbumArtAndUpdateNotification(String uriString, @NonNull Notification baseNotification) {
        // Kiểm tra ngay từ đầu, tránh gọi Glide không cần thiết
        if (player == null || currentSongIndex < 0 || currentSongIndex >= currentPlaylist.size() || uriString == null) {
            // Nếu uriString là null và ảnh hiện tại là ảnh mặc định thì không cần làm gì
            // Nếu uriString là null và ảnh hiện tại KHÔNG phải mặc định, cần cập nhật lại ảnh mặc định
            if (uriString == null && baseNotification.largeIcon != null) {
                Song songToUpdate = (currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()) ? currentPlaylist.get(currentSongIndex) : null;
                if(songToUpdate != null) {
                    Notification updatedNotification = createNotification(songToUpdate, null);
                    if(updatedNotification != null) notificationManager.notify(NOTIFICATION_ID, updatedNotification);
                }
            }
            return;
        }

        final Song songForArt = currentPlaylist.get(currentSongIndex); // Lấy bài hát tại thời điểm gọi
        if (songForArt == null) return; // Thêm kiểm tra null cho songForArt

        Log.d(TAG, "Loading album art: " + uriString + " for " + songForArt.getTitle());
        Glide.with(this).asBitmap().load(Uri.parse(uriString)).error(R.drawable.ic_album_24).override(256, 256).centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Chỉ cập nhật nếu player còn tồn tại VÀ bài hát vẫn là bài hát hiện tại
                        // *** SỬA Ở ĐÂY ***
                        if (player != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size() && Objects.equals(currentPlaylist.get(currentSongIndex).getAlbumArtPath(), uriString)) {
                            Log.d(TAG, "Art loaded successfully for " + songForArt.getTitle());
                            Notification updated = createNotification(songForArt, resource);
                            if(updated != null) notificationManager.notify(NOTIFICATION_ID, updated);
                        } else Log.w(TAG, "Art loaded but song/player changed: " + uriString);
                    }
                    @Override public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        // Chỉ cập nhật ảnh mặc định nếu player còn tồn tại VÀ bài hát vẫn là bài hát hiện tại VÀ ảnh hiện tại không phải mặc định
                        // *** SỬA Ở ĐÂY ***
                        if (player != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size() && Objects.equals(currentPlaylist.get(currentSongIndex).getAlbumArtPath(), uriString)) {
                            Log.w(TAG, "Art load failed for " + songForArt.getTitle());
                            if (baseNotification.largeIcon != null) { // Chỉ cập nhật nếu ảnh hiện tại không phải mặc định
                                Notification updated = createNotification(songForArt, null); // Đặt lại ảnh mặc định
                                if(updated != null) notificationManager.notify(NOTIFICATION_ID, updated);
                            }
                        } else Log.w(TAG, "Art load failed but song/player changed: " + uriString);
                    }
                    @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }
    // --- Kết thúc Notification Handling ---


    // --- Public Getters for Activity ---
    public Song getCurrentPlayingSong() {
        if (player != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()) {
            return currentPlaylist.get(currentSongIndex);
        } return null;
    }
    public boolean isPlaying() { return player != null && player.isPlaying(); }
    public long getPlayerDuration() {
        // Ưu tiên lấy từ ExoPlayer vì nó chính xác hơn
        if (player != null && player.isCurrentMediaItemSeekable() && player.getDuration() != C.TIME_UNSET) {
            return player.getDuration();
        }
        // Fallback về model nếu player chưa sẵn sàng
        else if (currentSongIndex >= 0 && currentSongIndex < currentPlaylist.size()) {
            return currentPlaylist.get(currentSongIndex).getDuration();
        }
        return 0;
    }
    public long getPlayerCurrentPosition() { return player != null ? player.getCurrentPosition() : 0; }
    public boolean getShuffleModeEnabled() { return player != null && player.getShuffleModeEnabled(); }
    public int getRepeatMode() { return player != null ? player.getRepeatMode() : Player.REPEAT_MODE_OFF; }
    // --- Kết thúc Public Getters ---

} // Kết thúc lớp MusicPlayerService