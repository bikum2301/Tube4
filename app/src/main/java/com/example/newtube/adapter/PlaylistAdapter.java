package com.example.newtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.Playlist; // Import model Playlist

import java.util.List;
import java.util.Locale; // Để format string

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<Playlist> playlistList;
    private Context context;

    public PlaylistAdapter(Context context, List<Playlist> playlistList) {
        this.context = context;
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);

        holder.tvTitle.setText(playlist.getTitle());
        // Kết hợp Creator và số bài hát cho subtitle
        String subtitle = String.format(Locale.getDefault(), "%s • %d bài hát",
                playlist.getCreator(), playlist.getSongCount());
        holder.tvSubtitle.setText(subtitle);

        // Load ảnh bằng Glide
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(8)); // Bo góc nhẹ

        Glide.with(context)
                .load(playlist.getThumbnailUrl()) // URL hoặc Resource ID
                .apply(requestOptions)
                .placeholder(R.color.placeholder_background_color)
                .error(R.drawable.ic_broken_image) // Đảm bảo bạn có drawable này
                .into(holder.ivThumbnail);

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Mở Playlist: " + playlist.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Implement logic mở màn hình chi tiết Playlist
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_playlist_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_playlist_title);
            tvSubtitle = itemView.findViewById(R.id.tv_playlist_subtitle);
        }
    }
}