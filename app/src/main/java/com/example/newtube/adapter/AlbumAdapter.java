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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners; // Có thể dùng hoặc không
import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.Album; // Import model Album

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private List<Album> albumList;
    private Context context;
    private OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    public AlbumAdapter(Context context, List<Album> albumList, OnAlbumClickListener listener) {
        this.context = context;
        this.albumList = albumList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albumList.get(position);

        holder.tvTitle.setText(album.getTitle());
        holder.tvArtist.setText(album.getArtist());

        // Load ảnh bìa album bằng Glide
        RequestOptions requestOptions = new RequestOptions();
        // Bo góc nhẹ cho khớp với CardView
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(8));

        Glide.with(context)
                .load(album.getAlbumArtUri()) // URI dạng String
                .apply(requestOptions)
                .placeholder(R.color.placeholder_background_color) // Placeholder màu
                .error(R.drawable.ic_album_24) // Tạo icon này: một đĩa CD/vinyl
                .into(holder.ivAlbumArt);

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlbumArt;
        TextView tvTitle;
        TextView tvArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.iv_album_art);
            tvTitle = itemView.findViewById(R.id.tv_album_title);
            tvArtist = itemView.findViewById(R.id.tv_album_artist);
        }
    }
}