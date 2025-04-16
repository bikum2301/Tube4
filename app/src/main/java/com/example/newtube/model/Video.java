package com.example.newtube.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Video implements Parcelable {

    // --- Các trường khớp với API Backend ---
    @SerializedName("_id")
    private String id; // MongoDB ID thường là String

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description; // Thêm nếu API có

    @SerializedName("duration")
    private long duration; // Milliseconds (giữ nguyên)

    @SerializedName("filePath") // Lưu URL gốc hoặc path cục bộ trên server (backend biết cách xử lý)
    private String filePath;

    @SerializedName("thumbnailPath") // Đường dẫn đến ảnh thumbnail
    private String thumbnailPath;

    @SerializedName("uploader") // Có thể là String hoặc Object ID tùy backend
    private String uploader; // Hoặc tạo lớp Uploader riêng

    @SerializedName("views")
    private int views; // Hoặc long

    // Constructor (cập nhật theo trường mới)
    public Video(String id, String title, String description, long duration, String filePath, String thumbnailPath, String uploader, int views) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
        this.uploader = uploader;
        this.views = views;
    }

    // --- Parcelable Implementation (Cập nhật) ---
    protected Video(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readLong();
        filePath = in.readString();
        thumbnailPath = in.readString();
        uploader = in.readString();
        views = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(duration);
        dest.writeString(filePath);
        dest.writeString(thumbnailPath);
        dest.writeString(uploader);
        dest.writeInt(views);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    // --- Getters (Cập nhật) ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getDuration() { return duration; }
    public String getFilePath() { return filePath; }
    public String getThumbnailPath() { return thumbnailPath; }
    public String getUploader() { return uploader; } // Hoặc trả về đối tượng Uploader
    public int getViews() { return views; }

    // --- Hàm tiện ích để lấy URL Streaming từ API ---
    public String getVideoStreamUrl() {
        // !! THAY THẾ BẰNG ĐỊA CHỈ IP VÀ CỔNG SERVER CỦA BẠN !!
        // Nên lấy từ một file cấu hình hoặc strings.xml
        String baseUrl = "http://192.168.1.28:3000"; // Ví dụ

        if (this.id == null || this.id.isEmpty()) {
            return null; // Không có ID, không thể tạo URL
        }
        return baseUrl + "/videos/" + this.id + "/stream";
    }

    // Hàm tiện ích để lấy thời lượng dạng MM:SS hoặc HH:MM:SS (giữ nguyên)
    public String getFormattedDuration() {
        if (duration <= 0) return "0:00"; // Sửa định dạng mặc định
        long totalSeconds = duration / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}