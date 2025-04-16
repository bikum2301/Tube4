package com.example.newtube.fragment.music;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;    // Import TextView cho thông báo lỗi/trống
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.adapter.SongAdapter;
import com.example.newtube.model.Song; // Model Song đã cập nhật
import com.example.newtube.network.ApiService;         // Import cho API
import com.example.newtube.network.RetrofitClient; // Import cho API
import com.example.newtube.service.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;     // Import cho Retrofit
import retrofit2.Callback; // Import cho Retrofit
import retrofit2.Response; // Import cho Retrofit

public class SongsFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private static final String TAG = "SongsFragment";

    // UI Components
    private RecyclerView rvSongs;
    private ProgressBar progressBar; // Để hiển thị trạng thái loading
    private TextView tvStatus;       // Để hiển thị thông báo (trống/lỗi)

    // Data & Adapters
    private SongAdapter songAdapter;
    private List<Song> songList;

    // Networking
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout mới (cần tạo layout này)
        View view = inflater.inflate(R.layout.fragment_songs_api, container, false); // Sử dụng layout mới
        progressBar = view.findViewById(R.id.progress_bar_songs);
        tvStatus = view.findViewById(R.id.tv_status_songs);
        rvSongs = view.findViewById(R.id.rv_songs); // RecyclerView vẫn giữ ID cũ
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songList = new ArrayList<>();
        // Sử dụng requireContext() để đảm bảo context hợp lệ
        songAdapter = new SongAdapter(requireContext(), songList, this);

        rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSongs.setAdapter(songAdapter);

        // Khởi tạo ApiService
        apiService = RetrofitClient.getApiService();

        // Tải dữ liệu từ API
        loadSongsFromApi();
    }

    // Hàm tải bài hát từ API
    private void loadSongsFromApi() {
        Log.d(TAG, "Attempting to load songs from API...");
        showLoading(true); // Hiển thị loading

        Call<List<Song>> call = apiService.getSongs();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                showLoading(false); // Ẩn loading
                if (!isAdded() || getContext() == null) return; // Kiểm tra Fragment còn tồn tại

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API call successful. Received " + response.body().size() + " songs.");
                    songList.clear();
                    songList.addAll(response.body());
                    songAdapter.notifyDataSetChanged();

                    if (songList.isEmpty()) {
                        showStatusMessage("Không có bài hát nào.");
                    } else {
                        showStatusMessage(null); // Ẩn thông báo nếu có dữ liệu
                    }
                } else {
                    Log.e(TAG, "API call failed. Code: " + response.code() + ", Message: " + response.message());
                    showStatusMessage("Lỗi tải danh sách nhạc (Code: " + response.code() + ")");
                    // Toast.makeText(getContext(), "Lỗi tải danh sách nhạc: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                showLoading(false); // Ẩn loading
                if (!isAdded() || getContext() == null) return; // Kiểm tra Fragment còn tồn tại

                Log.e(TAG, "API call failed onFailure: " + t.getMessage(), t);
                showStatusMessage("Lỗi kết nối mạng. Vui lòng thử lại.");
                // Toast.makeText(getContext(), "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper hiển thị/ẩn ProgressBar và RecyclerView
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (rvSongs != null) {
            rvSongs.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
        // Ẩn thông báo trạng thái khi đang load
        if (isLoading && tvStatus != null) {
            tvStatus.setVisibility(View.GONE);
        }
    }

    // Helper hiển thị thông báo trạng thái (trống hoặc lỗi)
    private void showStatusMessage(@Nullable String message) {
        if (tvStatus == null) return;
        if (message != null) {
            rvSongs.setVisibility(View.GONE); // Ẩn list khi có thông báo
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(message);
        } else {
            rvSongs.setVisibility(View.VISIBLE); // Hiện list khi không có thông báo
            tvStatus.setVisibility(View.GONE);
        }
    }


    // --- Triển khai OnSongClickListener (Logic gọi Service giữ nguyên) ---
    @Override
    public void onSongClick(Song song, int position) {
        if (getContext() == null || song == null) {
            Log.e(TAG, "onSongClick: Context or Song is null!");
            return;
        }
        // Kiểm tra xem songList có phần tử tại vị trí đó không
        if (position < 0 || position >= songList.size()) {
            Log.e(TAG, "onSongClick: Invalid position: " + position + " for list size: " + songList.size());
            return;
        }

        Log.d(TAG, "Song clicked: " + song.getTitle() + " at position: " + position);
        Intent serviceIntent = new Intent(requireContext(), MusicPlayerService.class);
        serviceIntent.setAction(MusicPlayerService.ACTION_SET_PLAYLIST_AND_PLAY);
        // Truyền ArrayList<Song> đã implement Parcelable
        serviceIntent.putParcelableArrayListExtra(MusicPlayerService.EXTRA_SONG_LIST, new ArrayList<>(songList));
        serviceIntent.putExtra(MusicPlayerService.EXTRA_START_POSITION, position);

        ContextCompat.startForegroundService(requireContext(), serviceIntent);
        Log.d(TAG, "Sent intent to start service and play");
    }

    @Override
    public void onSongOptionsClick(Song song, View anchorView) {
        showPopupMenu(anchorView, song);
    }

    // Hàm hiển thị PopupMenu (Logic bên trong giữ nguyên)
    private void showPopupMenu(View view, Song song) {
        if (getContext() == null || song == null) return;

        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.song_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (getContext() == null) return false;
            int itemId = item.getItemId();
            if (itemId == R.id.action_play_next) {
                Toast.makeText(getContext(), "Phát tiếp theo: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_add_to_queue) {
                Toast.makeText(getContext(), "Thêm vào hàng đợi: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_add_to_playlist) {
                Toast.makeText(getContext(), "Thêm vào playlist: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_song_details) {
                Toast.makeText(getContext(), "Chi tiết: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}