package com.example.newtube.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.newtube.R;
import com.example.newtube.adapter.ContentAdapter; // Import Adapter
import com.example.newtube.model.ContentItem;     // Import Model

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvRecentlyPlayed, rvMusicRecommendations, rvTrendingVideos, rvNewAlbums;
    private ContentAdapter recentlyPlayedAdapter, musicAdapter, videoAdapter, albumAdapter;
    private List<ContentItem> recentlyPlayedList, musicList, videoList, albumList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ RecyclerViews
        rvRecentlyPlayed = view.findViewById(R.id.rv_recently_played);
        rvMusicRecommendations = view.findViewById(R.id.rv_music_recommendations);
        rvTrendingVideos = view.findViewById(R.id.rv_trending_videos);
        rvNewAlbums = view.findViewById(R.id.rv_new_albums);

        // Khởi tạo dữ liệu (Tạm thời dùng dữ liệu giả)
        initData();

        // Thiết lập Adapter và LayoutManager cho từng RecyclerView
        setupRecyclerView(rvRecentlyPlayed, recentlyPlayedAdapter, recentlyPlayedList);
        setupRecyclerView(rvMusicRecommendations, musicAdapter, musicList);
        setupRecyclerView(rvTrendingVideos, videoAdapter, videoList);
        setupRecyclerView(rvNewAlbums, albumAdapter, albumList);
    }

    // Hàm helper để thiết lập RecyclerView ngang
    private void setupRecyclerView(RecyclerView recyclerView, ContentAdapter adapter, List<ContentItem> list) {
        adapter = new ContentAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        // Tùy chọn: Thêm ItemDecoration để tạo khoảng cách đều hơn giữa các item nếu muốn
        // recyclerView.addItemDecoration(...)
    }

    // Hàm tạo dữ liệu giả (Thay thế bằng dữ liệu thật từ API/Database sau này)
    private void initData() {
        recentlyPlayedList = new ArrayList<>();
        musicList = new ArrayList<>();
        videoList = new ArrayList<>();
        albumList = new ArrayList<>();

        // Dữ liệu giả - Thay bằng URL ảnh thật hoặc drawable resource
        String placeholderImage = "https://via.placeholder.com/300x260.png?text=Image"; // URL ảnh tạm
        String placeholderImage2 = "android.resource://" + getContext().getPackageName() + "/" + R.drawable.ic_launcher_background; // Ảnh từ drawable

        // --- Recently Played ---
        recentlyPlayedList.add(new ContentItem(placeholderImage, "Shape of You", "Ed Sheeran"));
        recentlyPlayedList.add(new ContentItem(placeholderImage2, "Blinding Lights", "The Weeknd"));
        recentlyPlayedList.add(new ContentItem(placeholderImage, "Levitating", "Dua Lipa"));
        recentlyPlayedList.add(new ContentItem(placeholderImage, "Watermelon Sugar", "Harry Styles"));


        // --- Music Recommendations ---
        musicList.add(new ContentItem(placeholderImage2, "Album: Future Nostalgia", "Dua Lipa"));
        musicList.add(new ContentItem(placeholderImage, "Bài hát: good 4 u", "Olivia Rodrigo"));
        musicList.add(new ContentItem(placeholderImage, "Playlist: Chill Hits", "Spotify"));
        musicList.add(new ContentItem(placeholderImage, "Album: After Hours", "The Weeknd"));
        musicList.add(new ContentItem(placeholderImage2, "Bài hát: Peaches", "Justin Bieber"));

        // --- Trending Videos ---
        videoList.add(new ContentItem(placeholderImage, "Video Hài Mới Nhất", "Kênh Giải Trí"));
        videoList.add(new ContentItem(placeholderImage2, "Hướng dẫn nấu ăn", "Kênh Ẩm Thực"));
        videoList.add(new ContentItem(placeholderImage, "MV Âm Nhạc Hot", "Kênh VEVO"));
        videoList.add(new ContentItem(placeholderImage, "Review Phim Bom Tấn", "Kênh Phim Ảnh"));
        videoList.add(new ContentItem(placeholderImage, "Gameplay Liên Quân", "Kênh Gaming"));
        videoList.add(new ContentItem(placeholderImage2, "Tin tức công nghệ", "Kênh Tech"));

        // --- New Albums ---
        albumList.add(new ContentItem(placeholderImage, "Album: Starboy", "The Weeknd"));
        albumList.add(new ContentItem(placeholderImage2, "Album: = (Equals)", "Ed Sheeran"));
        albumList.add(new ContentItem(placeholderImage, "Album: Positions", "Ariana Grande"));
        albumList.add(new ContentItem(placeholderImage, "Album: Justice", "Justin Bieber"));
    }
}