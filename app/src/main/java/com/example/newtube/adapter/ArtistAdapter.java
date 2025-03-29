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

import com.example.newtube.R;
import com.example.newtube.model.Artist; // Import model Artist

import java.util.List;
import java.util.Locale;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private List<Artist> artistList;
    private Context context;
    private OnArtistClickListener listener;

    public interface OnArtistClickListener {
        void onArtistClick(Artist artist);
    }

    public ArtistAdapter(Context context, List<Artist> artistList, OnArtistClickListener listener) {
        this.context = context;
        this.artistList = artistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = artistList.get(position);

        holder.tvArtistName.setText(artist.getName());
        // Format thông tin phụ
        String info = String.format(Locale.getDefault(), "%d Albums • %d Bài hát",
                artist.getNumberOfAlbums(), artist.getNumberOfTracks());
        holder.tvArtistInfo.setText(info);

        // Set icon mặc định (Không cần Glide vì chưa có ảnh nghệ sĩ từ MediaStore)
        holder.ivArtistIcon.setImageResource(R.drawable.ic_person_24); // Hoặc ic_mic_24

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArtistClick(artist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtistIcon;
        TextView tvArtistName;
        TextView tvArtistInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtistIcon = itemView.findViewById(R.id.iv_artist_icon);
            tvArtistName = itemView.findViewById(R.id.tv_artist_name);
            tvArtistInfo = itemView.findViewById(R.id.tv_artist_info);
        }
    }
}