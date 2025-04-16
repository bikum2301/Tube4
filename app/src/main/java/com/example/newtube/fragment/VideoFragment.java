package com.example.newtube.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.activity.VideoPlayer;
import com.example.newtube.adapter.VideoAdapter;
import com.example.newtube.network.RetrofitClient;
import com.example.newtube.network.ApiService;
import com.example.newtube.model.Video;
import com.example.newtube.model.VideoApiResponse; // *** THÊM IMPORT WRAPPER ***

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoFragment extends Fragment implements VideoAdapter.OnVideoClickListener {

    private static final String TAG = "VideoFragment";

    private RecyclerView rvVideos;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVideos = view.findViewById(R.id.rv_videos);
        progressBar = view.findViewById(R.id.progress_bar_videos);
        videoList = new ArrayList<>();
        if (getContext() != null) {
            videoAdapter = new VideoAdapter(getContext(), videoList, this);
            rvVideos.setLayoutManager(new LinearLayoutManager(getContext()));
            rvVideos.setAdapter(videoAdapter);
        } else {
            Log.e(TAG, "Context is null during onViewCreated.");
            return;
        }

        apiService = RetrofitClient.getApiService();
        fetchVideosFromApi();
    }

    private void fetchVideosFromApi() {
        if (progressBar == null || rvVideos == null || apiService == null) {
            Log.e(TAG, "UI/API not initialized in fetchVideosFromApi.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvVideos.setVisibility(View.GONE);

        // *** THAY ĐỔI KIỂU CALL ***
        Call<VideoApiResponse> call = apiService.getVideos();
        // *** THAY ĐỔI KIỂU CALLBACK ***
        call.enqueue(new Callback<VideoApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoApiResponse> call, @NonNull Response<VideoApiResponse> response) {
                if (!isAdded() || getContext() == null || progressBar == null || rvVideos == null || videoAdapter == null) {
                    return;
                }

                progressBar.setVisibility(View.GONE);
                rvVideos.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    // *** LẤY WRAPPER RESPONSE ***
                    VideoApiResponse apiResponse = response.body();

                    // *** KIỂM TRA TRƯỜNG SUCCESS VÀ LẤY DATA TỪ WRAPPER ***
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        videoList.clear();
                        videoList.addAll(apiResponse.getData()); // Lấy list từ apiResponse.getData()
                        videoAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Fetched " + videoList.size() + " videos from API.");
                        if (videoList.isEmpty()) {
                            Toast.makeText(getContext(), "Không có video nào.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Xử lý trường hợp success=false hoặc data=null từ API
                        Log.e(TAG, "API response indicates failure or missing data. Success: " + apiResponse.isSuccess());
                        Toast.makeText(getContext(), "Lỗi dữ liệu từ server.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Lỗi khi tải video: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoApiResponse> call, @NonNull Throwable t) { // Kiểu Call cũng thay đổi
                if (!isAdded() || getContext() == null || progressBar == null || rvVideos == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                rvVideos.setVisibility(View.VISIBLE);
                Log.e(TAG, "API call error: ", t);
                Toast.makeText(getContext(), "Lỗi mạng khi tải video.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- onVideoClick và các hàm khác giữ nguyên ---
    @Override
    public void onVideoClick(Video video) {
        if (getContext() == null) return;
        String streamUrl = video.getVideoStreamUrl();
        if (streamUrl != null) {
            Log.d(TAG, "Playing video: " + video.getTitle() + " URL: " + streamUrl);
            Intent intent = new Intent(getContext(), VideoPlayer.class);
            intent.setData(Uri.parse(streamUrl));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không thể lấy URL stream", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Stream URL is null for video: " + video.getTitle());
        }
    }

    @Override
    public void onVideoOptionsClick(Video video, View anchorView) {
        showPopupMenu(anchorView, video);
    }

    private void showPopupMenu(View view, Video video) {
        if (getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.video_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (getContext() == null) return false;
            int itemId = item.getItemId();
            if (itemId == R.id.action_play_video) {
                onVideoClick(video); return true;
            } else if (itemId == R.id.action_add_to_playlist) {
                Toast.makeText(getContext(), "Chức năng: Thêm video vào playlist...", Toast.LENGTH_SHORT).show(); return true;
            } else if (itemId == R.id.action_share_video) {
                String shareUrl = video.getVideoStreamUrl();
                if (shareUrl != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ video: " + video.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem video này: " + shareUrl);
                    startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
                } else {
                    Toast.makeText(getContext(), "Không thể chia sẻ video này", Toast.LENGTH_SHORT).show();
                } return true;
            } else if (itemId == R.id.action_video_details) {
                Toast.makeText(getContext(), "Chức năng: Chi tiết video...", Toast.LENGTH_SHORT).show(); return true;
            } else if (itemId == R.id.action_delete_video) {
                Toast.makeText(getContext(), "Chức năng: Xóa video...", Toast.LENGTH_SHORT).show(); return true;
            }
            return false;
        });
        popup.show();
    }
}