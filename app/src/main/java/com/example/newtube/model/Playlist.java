package com.example.newtube.model;

public class Playlist {
    private String thumbnailUrl;
    private String title;
    private String creator;
    private int songCount;

    public Playlist(String thumbnailUrl, String title, String creator, int songCount) {
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.creator = creator;
        this.songCount = songCount;
    }

    // Getters
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public int getSongCount() {
        return songCount;
    }
}