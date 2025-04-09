package com.example.newtube.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable { // Implement Parcelable
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String dataPath;
    private String albumArtUri;

    // Constructor
    public Song(long id, String title, String artist, String album, long duration, String dataPath, String albumArtUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.dataPath = dataPath;
        this.albumArtUri = albumArtUri;
    }

    // --- Parcelable Implementation ---

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readLong();
        dataPath = in.readString();
        albumArtUri = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(duration);
        dest.writeString(dataPath);
        dest.writeString(albumArtUri);
    }

    // --- Getters ---
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }
    public String getDataPath() { return dataPath; }
    public String getAlbumArtUri() { return albumArtUri; }

    // Hàm tiện ích để lấy thời lượng dạng MM:SS
    public String getFormattedDuration() {
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}