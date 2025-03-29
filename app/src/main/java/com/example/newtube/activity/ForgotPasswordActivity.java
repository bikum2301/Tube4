package com.example.newtube.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtube.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSubmit;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ Views
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Sự kiện nhấn nút Gửi Yêu Cầu
        btnSubmit.setOnClickListener(v -> attemptSubmit());

        // Sự kiện nhấn link Quay lại Đăng nhập
        tvBackToLogin.setOnClickListener(v -> {
            // Quay lại LoginActivity
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đóng màn hình quên mật khẩu
        });
    }

    private void attemptSubmit() {
        // Reset error
        tilEmail.setError(null);

        // Lấy giá trị email
        String email = etEmail.getText().toString().trim();

        // Kiểm tra Email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
        } else if (!isEmailValid(email)) {
            tilEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
        } else {
            // Email hợp lệ (ở đây chỉ hiện Toast, sau này sẽ xử lý gửi email thật)
            Toast.makeText(this, "Yêu cầu đặt lại mật khẩu đã được gửi tới " + email + " (Giả lập)", Toast.LENGTH_LONG).show();
            // Tùy chọn: Có thể quay lại Login sau khi gửi thành công
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    // Hàm kiểm tra định dạng email đơn giản (có thể tạo lớp Utils dùng chung)
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}