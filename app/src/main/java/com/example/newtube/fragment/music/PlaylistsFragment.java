package com.example.newtube.fragment.music;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LayoutManager
import androidx.recyclerview.widget.RecyclerView;      // Import RecyclerView
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.newtube.R;
import com.example.newtube.adapter.PlaylistAdapter; // Import Adapter
import com.example.newtube.model.Playlist;         // Import Model

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {

    private RecyclerView rvPlaylists;
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> playlistList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPlaylists = view.findViewById(R.id.rv_playlists);
        playlistList = new ArrayList<>();


        loadDummyPlaylists();


        playlistAdapter = new PlaylistAdapter(getContext(), playlistList);


        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaylists.setAdapter(playlistAdapter);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvPlaylists.getContext(),
                LinearLayoutManager.VERTICAL);
        rvPlaylists.addItemDecoration(dividerItemDecoration);

    }

    private void loadDummyPlaylists() {

        String placeholderImage1 = "https://via.placeholder.com/120x120.png?text=P1";
        String placeholderImage2 = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.ic_music_note; // Dùng icon nhạc làm ảnh tạm
        String placeholderImage3 = "https://via.placeholder.com/120x120.png?text=P3";

        playlistList.add(new Playlist(placeholderImage1, "Nhạc Chill Cuối Tuần", "Bởi bạn", 35));
        playlistList.add(new Playlist(placeholderImage2, "Top Hits V-Pop", "NewTube Music", 50));
        playlistList.add(new Playlist(placeholderImage3, "Acoustic Covers", "Bởi bạn", 18));
        playlistList.add(new Playlist(placeholderImage1, "Nhạc Chạy Bộ", "NewTube Fitness", 42));
        playlistList.add(new Playlist(placeholderImage2, "Indie Việt Hay Nhất", "Bởi bạn", 29));
        playlistList.add(new Playlist(placeholderImage3, "Nhạc Phim US-UK", "NewTube Music", 60));

        for (int i = 1; i <= 10; i++) {
            playlistList.add(new Playlist(placeholderImage1, "Playlist Tự Động " + i, "NewTube Music", 10 + i));
        }
    }
}