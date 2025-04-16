package com.example.newtube.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Import BuildConfig để kiểm tra debug/release
// import com.example.newtube.BuildConfig; // Bạn cần đảm bảo BuildConfig được tạo đúng

public class RetrofitClient {

    // *** THAY ĐỔI BASE_URL THEO ĐỊA CHỈ IP CỦA BẠN ***
    private static final String BASE_URL = "http://192.168.1.28:3000/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo Logging Interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            // Chỉ log chi tiết trong bản debug để tránh lộ thông tin nhạy cảm
            // if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            // } else {
            //    logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            // }

            // Cấu hình OkHttpClient với logger và timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS) // Timeout kết nối
                    .readTimeout(30, TimeUnit.SECONDS)    // Timeout đọc dữ liệu
                    .writeTimeout(30, TimeUnit.SECONDS)   // Timeout ghi dữ liệu
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Sử dụng OkHttpClient đã cấu hình
                    .addConverterFactory(GsonConverterFactory.create()) // Dùng Gson
                    .build();
        }
        return retrofit;
    }

    // Phương thức public để lấy instance ApiService (thread-safe hơn nếu cần)
    public static synchronized ApiService getApiService() { // Thêm synchronized
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }
}