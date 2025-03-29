package com.example.newtube.model;

public class Artist {
    private long id;
    private String name;
    private int numberOfAlbums;
    private int numberOfTracks;

    public Artist(long id, String name, int numberOfAlbums, int numberOfTracks) {
        this.id = id;
        this.name = name;
        this.numberOfAlbums = numberOfAlbums;
        this.numberOfTracks = numberOfTracks;
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public int getNumberOfAlbums() { return numberOfAlbums; }
    public int getNumberOfTracks() { return numberOfTracks; }
}