package com.example.newtube.fragment.music;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent; // Đã import
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log; // Import Log
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
import com.example.newtube.adapter.SongAdapter;
import com.example.newtube.model.Song;
import com.example.newtube.service.MusicPlayerService; // Import Service

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private static final String TAG = "SongsFragment"; // Tag cho Fragment này

    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList;

    // --- Phần xử lý quyền truy cập bộ nhớ ---
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadSongsFromMediaStore();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập bộ nhớ để tải nhạc", Toast.LENGTH_LONG).show();
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
        // Đảm bảo context không null khi tạo adapter, dùng requireContext() nếu chắc chắn fragment đã attach
        songAdapter = new SongAdapter(requireContext(), songList, this);

        rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSongs.setAdapter(songAdapter);

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        // Kiểm tra context trước khi yêu cầu quyền
        if (getContext() == null) return;

        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongsFromMediaStore();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(requireContext(), "Ứng dụng cần quyền truy cập âm thanh để hiển thị danh sách nhạc.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(permission);
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadSongsFromMediaStore() {
        // Kiểm tra context
        if (getContext() == null) return;

        songList.clear();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = null;
        try {
            cursor = requireContext().getContentResolver().query(musicUri, projection, selection, null, sortOrder);
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

                    Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                    songList.add(new Song(id, title, artist, album, duration, dataPath, albumArtUri.toString()));
                }
            } else {
                Log.e(TAG, "MediaStore query returned null cursor.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading songs from MediaStore.", e);
            if (isAdded()) Toast.makeText(getContext(), "Lỗi tải danh sách nhạc", Toast.LENGTH_SHORT).show();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        songAdapter.notifyDataSetChanged();

        if (songList.isEmpty() && isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "Không tìm thấy bài hát nào.", Toast.LENGTH_SHORT).show();
        } else if (!songList.isEmpty()){
            Log.d(TAG, "Loaded " + songList.size() + " songs.");
        }
    }

    // --- Triển khai OnSongClickListener ---

    @Override
    public void onSongClick(Song song, int position) {
        if (getContext() == null) return; // Kiểm tra context

        Log.d(TAG, "Song clicked: " + song.getTitle() + " at position: " + position);
        Intent serviceIntent = new Intent(requireContext(), MusicPlayerService.class);
        serviceIntent.setAction(MusicPlayerService.ACTION_SET_PLAYLIST_AND_PLAY);
        serviceIntent.putParcelableArrayListExtra(MusicPlayerService.EXTRA_SONG_LIST, new ArrayList<>(songList));
        serviceIntent.putExtra(MusicPlayerService.EXTRA_START_POSITION, position);

        ContextCompat.startForegroundService(requireContext(), serviceIntent);
        Log.d(TAG, "Sent intent to start service and play");
    }

    @Override
    public void onSongOptionsClick(Song song, View anchorView) {
        showPopupMenu(anchorView, song);
    }

    private void showPopupMenu(View view, Song song) {
        if (getContext() == null) return;

        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.song_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (getContext() == null) return false;

            int itemId = item.getItemId();
            // ... (xử lý các action khác của popup menu) ...
            if (itemId == R.id.action_play_next) {
                Toast.makeText(getContext(), "Phát tiếp theo: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } // ... các else if khác ...

            return false;
        });
        popup.show();
    }
}