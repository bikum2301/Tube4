package com.example.newtube.adapter;

import android.content.Context;
import android.net.Uri; // Import Uri
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu; // Import PopupMenu
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.Song; // Import model Song

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songList;
    private Context context;
    // (Tùy chọn) Interface để xử lý sự kiện click
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
        void onSongOptionsClick(Song song, View anchorView);
    }

    public SongAdapter(Context context, List<Song> songList, OnSongClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener; // Nhận listener từ Fragment
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        // Load ảnh bìa album bằng Glide
        RequestOptions requestOptions = new RequestOptions();
        // Không bo góc hoặc bo rất nhẹ cho ảnh thumbnail vuông nhỏ
        requestOptions = requestOptions.transforms(new CenterCrop()); // , new RoundedCorners(4)

        Glide.with(context)
                .load(song.getAlbumArtPath()) // Có thể là URI hoặc URL
                .apply(requestOptions)
                .placeholder(R.drawable.ic_music_note_24) // Dùng icon nhạc làm placeholder
                .error(R.drawable.ic_music_note_24) // Cũng dùng icon nhạc khi lỗi
                .into(holder.ivThumbnail);

        // Sự kiện click vào cả hàng (để phát nhạc)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song, holder.getAdapterPosition());
            }
        });

        // Sự kiện click vào nút Options
        holder.btnOptions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongOptionsClick(song, holder.btnOptions);
            }
            // Hoặc xử lý trực tiếp PopupMenu ở đây nếu không cần Fragment can thiệp nhiều
            // showPopupMenu(holder.btnOptions, song);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    // (Tùy chọn) Hàm hiển thị PopupMenu - Có thể đặt ở đây hoặc trong Fragment
    private void showPopupMenu(View view, Song song) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.song_options_menu, popup.getMenu()); // Tạo menu này sau
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_play_next) {
                Toast.makeText(context, "Chức năng: Phát tiếp theo - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_add_to_queue) {
                Toast.makeText(context, "Chức năng: Thêm vào hàng đợi - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_add_to_playlist) {
                Toast.makeText(context, "Chức năng: Thêm vào playlist - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_song_details) {
                Toast.makeText(context, "Chức năng: Chi tiết - " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
            // Thêm các action khác: Xóa, Chia sẻ...
            return false;
        });
        popup.show();
    }


    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvArtist;
        ImageButton btnOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_song_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            btnOptions = itemView.findViewById(R.id.btn_song_options);
        }
    }
}