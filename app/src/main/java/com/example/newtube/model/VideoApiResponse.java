package com.example.newtube.model; // Hoặc package network.response

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<Video> data; // Danh sách các đối tượng Video

    // --- Getters ---
    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<Video> getData() {
        return data;
    }

    // (Optional) Thêm setters hoặc constructor nếu cần
}