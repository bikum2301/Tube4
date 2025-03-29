package com.example.newtube.fragment.music; // **ĐẢM BẢO ĐÚNG PACKAGE**

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.adapter.ArtistAdapter; // Import Adapter
import com.example.newtube.model.Artist;         // Import Model

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment implements ArtistAdapter.OnArtistClickListener {

    private RecyclerView rvArtists;
    private ArtistAdapter artistAdapter;
    private List<Artist> artistList;

    // --- Phần xử lý quyền (Giống các Fragment trước) ---
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadArtistsFromMediaStore();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập bộ nhớ để tải danh sách nghệ sĩ", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvArtists = view.findViewById(R.id.rv_artists);
        artistList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(getContext(), artistList, this);

        rvArtists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvArtists.setAdapter(artistAdapter);

        // (Tùy chọn) Thêm đường kẻ phân cách
        /*
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvArtists.getContext(),
                LinearLayoutManager.VERTICAL);
        rvArtists.addItemDecoration(dividerItemDecoration);
        */

        checkAndRequestPermissions();
    }

    // --- Hàm kiểm tra quyền (Giống các Fragment trước) ---
    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadArtistsFromMediaStore();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập âm thanh để hiển thị danh sách nghệ sĩ.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(permission);
        }
        else {
            requestPermissionLauncher.launch(permission);
        }
    }


    // Hàm load artists từ MediaStore
    private void loadArtistsFromMediaStore() {
        artistList.clear();
        // Uri cho Artists
        Uri artistUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        // Các cột cần lấy
        String[] projection = {
                MediaStore.Audio.Artists._ID,           // ID nghệ sĩ
                MediaStore.Audio.Artists.ARTIST,        // Tên nghệ sĩ
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS, // Số lượng album
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS  // Số lượng bài hát
        };
        // Sắp xếp theo tên nghệ sĩ
        String sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC";

        Cursor cursor = requireContext().getContentResolver().query(artistUri, projection, null, null, sortOrder);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
            int numAlbumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int numTracksColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int numAlbums = cursor.getInt(numAlbumsColumn);
                int numTracks = cursor.getInt(numTracksColumn);

                // Lọc bỏ các nghệ sĩ không tên hoặc <unknown>
                if (name != null && !name.equalsIgnoreCase("<unknown>")) {
                    artistList.add(new Artist(id, name, numAlbums, numTracks));
                }
            }
            cursor.close();
        }

        artistAdapter.notifyDataSetChanged();

        if(artistList.isEmpty()){
            Toast.makeText(getContext(), "Không tìm thấy nghệ sĩ nào trên thiết bị.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Triển khai OnArtistClickListener ---
    @Override
    public void onArtistClick(Artist artist) {
        Toast.makeText(getContext(), "Mở Nghệ sĩ: " + artist.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Chuyển sang màn hình chi tiết Nghệ sĩ (ArtistDetailActivity/Fragment)
        // Intent intent = new Intent(getContext(), ArtistDetailActivity.class);
        // intent.putExtra("ARTIST_ID", artist.getId());
        // intent.putExtra("ARTIST_NAME", artist.getName());
        // startActivity(intent);
    }
}