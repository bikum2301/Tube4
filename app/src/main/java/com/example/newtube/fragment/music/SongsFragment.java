package com.example.newtube.fragment.music; // **ĐẢM BẢO ĐÚNG PACKAGE**

import android.Manifest; // Import Manifest
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Import ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts; // Import ActivityResultContracts
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtube.R;
import com.example.newtube.adapter.SongAdapter; // Import Adapter
import com.example.newtube.model.Song;         // Import Model

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment implements SongAdapter.OnSongClickListener { // Implement Interface

    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList;

    // --- Phần xử lý quyền truy cập bộ nhớ ---
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Quyền được cấp, load nhạc
                    loadSongsFromMediaStore();
                } else {
                    // Quyền bị từ chối, hiển thị thông báo
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập bộ nhớ để tải nhạc", Toast.LENGTH_LONG).show();
                    // Có thể hiển thị một TextView trong layout yêu cầu cấp quyền
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSongs = view.findViewById(R.id.rv_songs);
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList, this); // Truyền this (Fragment) làm listener

        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSongs.setAdapter(songAdapter);

        // Kiểm tra và yêu cầu quyền truy cập bộ nhớ
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            // Android dưới 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp
            loadSongsFromMediaStore();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            // Giải thích tại sao cần quyền (có thể hiển thị dialog)
            Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập âm thanh để hiển thị danh sách nhạc.", Toast.LENGTH_LONG).show();
            // Yêu cầu lại quyền sau khi giải thích
            requestPermissionLauncher.launch(permission);
        }
        else {
            // Chưa có quyền, yêu cầu lần đầu hoặc người dùng đã từ chối vĩnh viễn
            requestPermissionLauncher.launch(permission);
        }
    }


    // Hàm load nhạc từ MediaStore (Cần quyền READ_EXTERNAL_STORAGE hoặc READ_MEDIA_AUDIO)
    private void loadSongsFromMediaStore() {
        songList.clear(); // Xóa danh sách cũ trước khi load lại
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, // Đường dẫn file
                MediaStore.Audio.Media.ALBUM_ID
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"; // Chỉ lấy file nhạc
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC"; // Sắp xếp theo tên A-Z

        Cursor cursor = requireContext().getContentResolver().query(musicUri, projection, selection, null, sortOrder);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String album = cursor.getString(albumColumn);
                long duration = cursor.getLong(durationColumn);
                String dataPath = cursor.getString(dataColumn);
                long albumId = cursor.getLong(albumIdColumn);

                // Lấy Uri ảnh bìa album
                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                songList.add(new Song(id, title, artist, album, duration, dataPath, albumArtUri.toString()));
            }
            cursor.close();
        }

        // Thông báo cho adapter biết dữ liệu đã thay đổi
        songAdapter.notifyDataSetChanged();

        if(songList.isEmpty()){
            Toast.makeText(getContext(), "Không tìm thấy bài hát nào trên thiết bị.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Triển khai các phương thức từ Interface OnSongClickListener ---

    @Override
    public void onSongClick(Song song, int position) {
        // Xử lý khi nhấn vào một bài hát (ví dụ: phát nhạc)
        Toast.makeText(getContext(), "Phát bài hát: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Gọi đến service/player để phát nhạc, truyền danh sách bài hát và vị trí bắt đầu
        // Intent serviceIntent = new Intent(getContext(), MusicPlayerService.class);
        // serviceIntent.putExtra("SONG_LIST", new ArrayList<>(songList)); // Truyền danh sách (cần Serializable hoặc Parcelable)
        // serviceIntent.putExtra("START_POSITION", position);
        // requireContext().startService(serviceIntent);
    }

    @Override
    public void onSongOptionsClick(Song song, View anchorView) {
        // Hiển thị PopupMenu khi nhấn nút options
        showPopupMenu(anchorView, song);
    }

    // Hàm hiển thị PopupMenu (đặt trong Fragment để dễ truy cập context và xử lý action phức tạp hơn)
    private void showPopupMenu(View view, Song song) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.song_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_play_next) {
                Toast.makeText(getContext(), "Chức năng: Phát tiếp theo - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Thêm logic Phát tiếp theo
                return true;
            } else if (itemId == R.id.action_add_to_queue) {
                Toast.makeText(getContext(), "Chức năng: Thêm vào hàng đợi - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Thêm logic Thêm vào hàng đợi
                return true;
            } else if (itemId == R.id.action_add_to_playlist) {
                Toast.makeText(getContext(), "Chức năng: Thêm vào playlist - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Hiển thị Dialog/Activity chọn Playlist
                return true;
            } else if (itemId == R.id.action_song_details) {
                Toast.makeText(getContext(), "Chức năng: Chi tiết - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Hiển thị Dialog/Activity chi tiết bài hát
                return true;
            }
            return false;
        });
        popup.show();
    }
}