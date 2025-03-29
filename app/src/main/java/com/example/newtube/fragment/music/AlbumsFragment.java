package com.example.newtube.fragment.music; // **ĐẢM BẢO ĐÚNG PACKAGE**

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration; // Import Configuration
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics; // Import DisplayMetrics
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager; // Import GridLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.adapter.AlbumAdapter; // Import Adapter
import com.example.newtube.model.Album;         // Import Model

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private RecyclerView rvAlbums;
    private AlbumAdapter albumAdapter;
    private List<Album> albumList;

    // --- Phần xử lý quyền (Giống SongsFragment) ---
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadAlbumsFromMediaStore();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập bộ nhớ để tải albums", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlbums = view.findViewById(R.id.rv_albums);
        albumList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(getContext(), albumList, this);

        // --- Thiết lập GridLayoutManager ---
        int spanCount = calculateSpanCount(requireContext()); // Tính số cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        rvAlbums.setLayoutManager(layoutManager);
        //------------------------------------

        rvAlbums.setAdapter(albumAdapter);
        // (Tùy chọn) Thêm ItemDecoration để tạo khoảng cách đều
        // rvAlbums.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingInPixels, includeEdge));

        checkAndRequestPermissions();
    }

    // --- Hàm kiểm tra quyền (Giống SongsFragment) ---
    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadAlbumsFromMediaStore();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập âm thanh để hiển thị danh sách albums.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(permission);
        }
        else {
            requestPermissionLauncher.launch(permission);
        }
    }


    // Hàm load albums từ MediaStore
    private void loadAlbumsFromMediaStore() {
        albumList.clear();
        // Uri cho Album
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        // Các cột cần lấy
        String[] projection = {
                MediaStore.Audio.Albums._ID,          // ID của album
                MediaStore.Audio.Albums.ALBUM,        // Tên album
                MediaStore.Audio.Albums.ARTIST,       // Tên nghệ sĩ (của album)
                MediaStore.Audio.Albums.NUMBER_OF_SONGS // Số lượng bài hát trong album
                // MediaStore.Audio.Albums.ALBUM_ART // Cột này có thể null hoặc không đáng tin cậy
        };
        // Sắp xếp theo tên Album
        String sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC";

        Cursor cursor = requireContext().getContentResolver().query(albumUri, projection, null, null, sortOrder);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            int numSongsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            // int artColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART); // Lấy index nếu dùng

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                int numSongs = cursor.getInt(numSongsColumn);
                // String albumArtPath = cursor.getString(artColumn); // Lấy đường dẫn nếu dùng

                // Tạo Uri ảnh bìa từ ID album (Cách này đáng tin cậy hơn)
                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);

                albumList.add(new Album(id, title, artist, albumArtUri.toString(), numSongs));
            }
            cursor.close();
        }

        albumAdapter.notifyDataSetChanged();

        if(albumList.isEmpty()){
            Toast.makeText(getContext(), "Không tìm thấy album nào trên thiết bị.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Tính toán số cột cho GridLayout ---
    public static int calculateSpanCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int itemWidthDp = 160; // Chiều rộng ước tính của một item album (bao gồm margin) - Điều chỉnh nếu cần
        int spanCount = (int) (dpWidth / itemWidthDp);
        if (spanCount < 2) { // Ít nhất là 2 cột
            // Kiểm tra nếu màn hình ngang thì có thể cho 3 hoặc 4 cột
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 3; // Hoặc 4 tùy ý
            }
            return 2;
        }
        return spanCount;
    }

    // --- Triển khai OnAlbumClickListener ---
    @Override
    public void onAlbumClick(Album album) {
        Toast.makeText(getContext(), "Mở Album: " + album.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Chuyển sang màn hình chi tiết Album (AlbumDetailActivity/Fragment)
        // Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
        // intent.putExtra("ALBUM_ID", album.getId());
        // intent.putExtra("ALBUM_TITLE", album.getTitle());
        // startActivity(intent);
    }
}