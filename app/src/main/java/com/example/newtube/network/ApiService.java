package com.example.newtube.network;

import com.example.newtube.model.Album;
import com.example.newtube.model.Artist;
import com.example.newtube.model.Playlist;
import com.example.newtube.model.Song;
import com.example.newtube.model.VideoApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    // --- Songs ---
    @GET("songs")
    Call<List<Song>> getSongs();

    // --- Albums ---
    @GET("albums")
    Call<List<Album>> getAlbums();

    // --- Artists ---
    @GET("artists")
    Call<List<Artist>> getArtists();

    // --- Playlists ---
    @GET("playlists")
    Call<List<Playlist>> getPlaylists();

    // --- Videos ---
    @GET("videos")
    Call<VideoApiResponse> getVideos(); // *** THAY ĐỔI KIỂU TRẢ VỀ ***

    // Ví dụ Login/Register
    /*
    @POST("users/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @POST("users/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
    */

}