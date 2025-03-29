package com.example.newtube.model;

public class Album {
    private long id;
    private String title;
    private String artist;
    private String albumArtUri; // URI ảnh bìa (dạng String)
    private int numberOfSongs;

    public Album(long id, String title, String artist, String albumArtUri, int numberOfSongs) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.numberOfSongs = numberOfSongs;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbumArtUri() { return albumArtUri; }
    public int getNumberOfSongs() { return numberOfSongs; }
}