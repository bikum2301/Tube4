package com.example.newtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.Video; // Import model Video

import java.util.List;

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
        holder.tvSubtitle.setText(video.getFolderName()); // Hiển thị tên thư mục làm phụ đề

        // Load thumbnail video bằng Glide
        // Glide có thể tự động lấy thumbnail từ Uri của video
        RequestOptions requestOptions = new RequestOptions()
                .frame(1000000) // Lấy frame ở giây thứ 1 (1,000,000 microseconds) làm thumbnail
                .centerCrop();

        Glide.with(context)
                .load(video.getVideoUri()) // Load từ Uri của video
                .apply(requestOptions)
                .placeholder(R.color.placeholder_background_color)
                .error(R.drawable.ic_broken_image) // Icon lỗi chung
                .into(holder.ivThumbnail);

        // Set icon kênh mặc định (chưa có dữ liệu thật)
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

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvDuration;
        ImageView ivChannelIcon; // Tạm thời chưa dùng nhiều
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
}