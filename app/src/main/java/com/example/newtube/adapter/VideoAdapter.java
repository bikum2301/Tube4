package com.example.newtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu; // Giữ lại
import android.widget.TextView;
import android.widget.Toast; // Giữ lại

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
// Bỏ RequestOptions nếu không cần frame cụ thể nữa
// import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.Video;

import java.util.List;
import java.util.Locale; // Import Locale

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<Video> videoList;
    private Context context;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
        void onVideoOptionsClick(Video video, View anchorView);
    }

    public VideoAdapter(Context context, List<Video> videoList, OnVideoClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videoList.get(position);

        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getFormattedDuration());

        // --- CẬP NHẬT PHẦN HIỂN THỊ PHỤ ĐỀ ---
        // Hiển thị tên người upload và lượt xem (ví dụ)
        String uploader = video.getUploader(); // Lấy tên người upload (hoặc ID)
        int views = video.getViews();
        String subtitleText = (uploader != null ? uploader : "Unknown Uploader") + " • " + formatViews(views) + " lượt xem";
        holder.tvSubtitle.setText(subtitleText);

        // --- CẬP NHẬT LOAD THUMBNAIL TỪ API ---
        // Load thumbnail từ thumbnailPath do API cung cấp
        Glide.with(context)
                .load(video.getThumbnailPath()) // Sử dụng thumbnailPath
                .placeholder(R.color.placeholder_background_color) // Màu nền chờ load
                .error(R.drawable.ic_broken_image) // Icon lỗi
                .centerCrop() // Hoặc fitCenter() tùy ý
                .into(holder.ivThumbnail);

        // Set icon kênh mặc định (có thể load ảnh người upload sau này)
        holder.ivChannelIcon.setImageResource(R.drawable.ic_person_24);

        // Sự kiện click vào cả hàng (để phát video)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video);
            }
        });

        // Sự kiện click vào nút Options
        holder.btnOptions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoOptionsClick(video, holder.btnOptions);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // --- ViewHolder Class (Giữ nguyên) ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvDuration;
        ImageView ivChannelIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageButton btnOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            tvDuration = itemView.findViewById(R.id.tv_video_duration);
            ivChannelIcon = itemView.findViewById(R.id.iv_channel_icon);
            tvTitle = itemView.findViewById(R.id.tv_video_title);
            tvSubtitle = itemView.findViewById(R.id.tv_video_subtitle);
            btnOptions = itemView.findViewById(R.id.btn_video_options);
        }
    }

    // --- Hàm tiện ích định dạng lượt xem (ví dụ) ---
    private String formatViews(int views) {
        if (views < 1000) {
            return String.valueOf(views);
        } else if (views < 1000000) {
            return String.format(Locale.getDefault(), "%.1f N", views / 1000.0); // N = Nghìn
        } else {
            return String.format(Locale.getDefault(), "%.1f Tr", views / 1000000.0); // Tr = Triệu
        }
    }
}