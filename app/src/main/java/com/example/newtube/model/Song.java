package com.example.newtube.model;

public class Song {
    private long id; // ID từ MediaStore hoặc cơ sở dữ liệu
    private String title;
    private String artist;
    private String album;
    private long duration; // Milliseconds
    private String dataPath; // Đường dẫn file hoặc URL
    private String albumArtUri; // URI hoặc URL ảnh bìa album

    public Song(long id, String title, String artist, String album, long duration, String dataPath, String albumArtUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.dataPath = dataPath;
        this.albumArtUri = albumArtUri;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }
    public String getDataPath() { return dataPath; }
    public String getAlbumArtUri() { return albumArtUri; }

    // Hàm tiện ích để lấy thời lượng dạng MM:SS (Tùy chọn)
    public String getFormattedDuration() {
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}