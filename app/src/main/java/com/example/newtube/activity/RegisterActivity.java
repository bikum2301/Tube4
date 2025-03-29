package com.example.newtube.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Thêm import TextUtils
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Optional: for back button in ActionBar

import com.example.newtube.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);


        btnRegister.setOnClickListener(v -> attemptRegister());


        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        // Reset errors
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Lấy giá trị input
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Kiểm tra Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            focusView = etConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            focusView = etConfirmPassword;
            cancel = true;
        }

        // Kiểm tra Password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            focusView = etPassword;
            cancel = true;
        } else if (password.length() < 6) { // Thêm điều kiện độ dài tối thiểu nếu muốn
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            focusView = etPassword;
            cancel = true;
        }

        // Kiểm tra Email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tilEmail.setError("Email không hợp lệ");
            focusView = etEmail;
            cancel = true;
        }

        // Kiểm tra Username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Vui lòng nhập tên người dùng");
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            // Có lỗi, focus vào field lỗi đầu tiên
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Dữ liệu hợp lệ (ở đây chỉ hiện Toast, sau này sẽ xử lý đăng ký thật)
            Toast.makeText(this, "Đăng ký thành công (Giả lập)", Toast.LENGTH_SHORT).show();
            // Tùy chọn: Tự động chuyển về màn hình Login sau khi đăng ký thành công
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đóng màn hình đăng ký
        }
    }

    // Hàm kiểm tra định dạng email đơn giản
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}