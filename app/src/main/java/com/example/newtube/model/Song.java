package com.example.newtube.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName; // Import cho Gson

public class Song implements Parcelable {

    @SerializedName("_id") // Khớp với MongoDB _id
    private String id;     // Đổi thành String

    @SerializedName("title")
    private String title;

    @SerializedName("artist")
    private String artist;

    @SerializedName("album")
    private String album;

    @SerializedName("duration") // Giả sử API trả về milliseconds
    private long duration;

    @SerializedName("filePath") // Khớp với API
    private String filePath;    // Đổi tên từ dataPath

    @SerializedName("albumArtPath") // Khớp với API
    private String albumArtPath;   // Đổi tên từ albumArtUri

    // Constructor cập nhật cho API
    public Song(String id, String title, String artist, String album, long duration, String filePath, String albumArtPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.filePath = filePath;
        this.albumArtPath = albumArtPath;
    }

    // --- Parcelable Implementation (Cập nhật theo trường mới) ---
    protected Song(Parcel in) {
        id = in.readString(); // Đọc String id
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readLong();
        filePath = in.readString(); // Đọc filePath
        albumArtPath = in.readString(); // Đọc albumArtPath
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id); // Ghi String id
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(duration);
        dest.writeString(filePath); // Ghi filePath
        dest.writeString(albumArtPath); // Ghi albumArtPath
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override public Song createFromParcel(Parcel in) { return new Song(in); }
        @Override public Song[] newArray(int size) { return new Song[size]; }
    };

    @Override public int describeContents() { return 0; }
    // --- Kết thúc Parcelable ---

    // --- Getters (Cập nhật) ---
    public String getId() { return id; } // Trả về String
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }
    public String getFilePath() { return filePath; }
    public String getAlbumArtPath() { return albumArtPath; }

    // Hàm format thời gian (giữ nguyên)
    public String getFormattedDuration() {
        if (duration <= 0) return "00:00"; // Xử lý trường hợp duration không hợp lệ
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}