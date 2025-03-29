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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.newtube.R;
import com.example.newtube.model.ContentItem;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private List<ContentItem> itemList;
    private Context context;

    public ContentAdapter(Context context, List<ContentItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContentItem item = itemList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());

        // Sử dụng Glide để load ảnh từ URL (hoặc resource nếu là dữ liệu local)
        Glide.with(context)
                .load(item.getImageUrl()) // URL hoặc Resource ID
                .apply(new RequestOptions().transform(new RoundedCorners(1))) // Bo góc nhẹ cho ảnh khớp CardView
                .placeholder(R.color.placeholder_background_color) // Ảnh tạm thời khi đang load
                .error(R.drawable.ic_broken_image) // Ảnh khi lỗi (tạo drawable ic_broken_image)
                .into(holder.ivImage);

        // Xử lý sự kiện click vào item (Tạm thời chỉ hiện Toast)
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            // Sau này sẽ mở màn hình chi tiết bài hát/video ở đây
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
        TextView tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_item_image);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvSubtitle = itemView.findViewById(R.id.tv_item_subtitle);
        }
    }
}