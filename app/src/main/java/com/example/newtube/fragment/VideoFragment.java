package com.example.newtube.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.activity.VideoPlayer; // *** THÊM IMPORT NÀY ***
import com.example.newtube.adapter.VideoAdapter;
import com.example.newtube.model.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment implements VideoAdapter.OnVideoClickListener {

    private static final String TAG = "VideoFragment"; // Tag cho Log

    private RecyclerView rvVideos;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;

    // --- Phần xử lý quyền truy cập video ---
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadVideosFromMediaStore();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập video để hiển thị", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVideos = view.findViewById(R.id.rv_videos);
        videoList = new ArrayList<>();
        // Khởi tạo listener trước khi gán adapter
        videoAdapter = new VideoAdapter(getContext(), videoList, this);

        rvVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideos.setAdapter(videoAdapter); // Gán adapter ngay cả khi list đang rỗng

        checkAndRequestPermissions();
    }

    // --- Hàm kiểm tra quyền truy cập video ---
    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadVideosFromMediaStore();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập video để hiển thị danh sách.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(permission);
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    // Hàm load video từ MediaStore
    private void loadVideosFromMediaStore() {
        videoList.clear();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME, // Tên file video (thường dùng làm title)
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA, // Đường dẫn file
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME // Tên thư mục chứa video
        };
        // Sắp xếp theo ngày thêm mới nhất
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null; // Khởi tạo cursor là null
        try { // Sử dụng try-catch-finally để đảm bảo cursor luôn đóng
            cursor = requireContext().getContentResolver().query(videoUri, projection, null, null, sortOrder);

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    long duration = cursor.getLong(durationColumn);
                    String dataPath = cursor.getString(dataColumn);
                    String folderName = cursor.getString(folderColumn);

                    // Kiểm tra xem file có tồn tại không trước khi thêm vào danh sách
                    if (dataPath != null) {
                        File videoFile = new File(dataPath);
                        if (videoFile.exists()) {
                            videoList.add(new Video(id, title, duration, dataPath, folderName));
                            // Bỏ Log ở đây để tránh spam Logcat quá nhiều
                            // Log.d(TAG, "Loaded video: " + title + " from " + folderName);
                        } else {
                            Log.w(TAG, "Video file not found: " + dataPath);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Cursor is null, failed to query MediaStore for videos.");
            }
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra khi query
            Log.e(TAG, "Error loading videos from MediaStore", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Đảm bảo cursor được đóng
            }
        }


        videoAdapter.notifyDataSetChanged(); // Cập nhật UI trên main thread

        if (videoList.isEmpty()) {
            Log.d(TAG, "No videos found on device or failed to load.");
            // Chỉ hiện Toast nếu fragment còn được gắn vào context
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Không tìm thấy video nào trên thiết bị.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Loaded " + videoList.size() + " videos.");
        }
    }

    // --- Triển khai OnVideoClickListener ---
    @Override
    public void onVideoClick(Video video) {
        // ** BẮT ĐẦU THAY ĐỔI **
        Log.d(TAG, "Attempting to play video: " + video.getTitle() + " URI: " + video.getVideoUri());

        // Tạo Intent để mở VideoPlayerActivity
        Intent intent = new Intent(getContext(), VideoPlayer.class);
        // Đặt Uri của video làm dữ liệu cho Intent
        intent.setData(video.getVideoUri());

        // Khởi chạy Activity
        startActivity(intent);
        // ** KẾT THÚC THAY ĐỔI **
    }

    @Override
    public void onVideoOptionsClick(Video video, View anchorView) {
        showPopupMenu(anchorView, video);
    }

    // Hàm hiển thị PopupMenu cho video
    private void showPopupMenu(View view, Video video) {
        // Kiểm tra context trước khi tạo PopupMenu
        if (getContext() == null) return;

        PopupMenu popup = new PopupMenu(requireContext(), view); // Dùng requireContext cho an toàn
        popup.getMenuInflater().inflate(R.menu.video_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            // Kiểm tra context trước khi thực hiện action
            if (getContext() == null) return false;

            int itemId = item.getItemId();
            if (itemId == R.id.action_play_video) {
                onVideoClick(video); // Gọi lại hàm play video
                return true;
            } else if (itemId == R.id.action_add_to_playlist) {
                Toast.makeText(getContext(), "Chức năng: Thêm video vào playlist...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_share_video) {
                Toast.makeText(getContext(), "Chức năng: Chia sẻ video...", Toast.LENGTH_SHORT).show();
                // TODO: Tạo intent share video (ACTION_SEND)
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/*"); // Loại MIME cho video
                shareIntent.putExtra(Intent.EXTRA_STREAM, video.getVideoUri());
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ video: " + video.getTitle()); // Tiêu đề email/tin nhắn (tùy chọn)
                // shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem video này!"); // Nội dung text (tùy chọn)
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ video qua"));
                return true;
            } else if (itemId == R.id.action_video_details) {
                Toast.makeText(getContext(), "Chức năng: Chi tiết video...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete_video) {
                Toast.makeText(getContext(), "Chức năng: Xóa video...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}