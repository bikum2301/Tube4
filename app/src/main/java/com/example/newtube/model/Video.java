package com.example.newtube.model;

import android.net.Uri;
import android.provider.MediaStore;

public class Video {
    private long id;
    private String title;
    private long duration; // Milliseconds
    private String dataPath; // Đường dẫn file
    private String folderName; // Tên thư mục chứa video

    public Video(long id, String title, long duration, String dataPath, String folderName) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.dataPath = dataPath;
        this.folderName = folderName;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public long getDuration() { return duration; }
    public String getDataPath() { return dataPath; }
    public String getFolderName() { return folderName; }

    // Hàm tiện ích để lấy Uri của video
    public Uri getVideoUri() {
        return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
    }

    // Hàm tiện ích để lấy thời lượng dạng MM:SS hoặc HH:MM:SS
    public String getFormattedDuration() {
        long totalSeconds = duration / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}