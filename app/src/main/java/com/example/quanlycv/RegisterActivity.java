package com.example.quanlycv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.User;
import com.example.quanlycv.utils.SharedPrefManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegFullName, etRegEmail, etRegPassword;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        prefManager = new SharedPrefManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etRegFullName = findViewById(R.id.etRegFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLinkLogin = findViewById(R.id.tvLinkLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvLinkLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String fullName = etRegFullName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(email.split("@")[0], email, password, fullName);
        long newId = dbHelper.registerUser(newUser);

        if (newId > 0) {
            prefManager.saveUserLogin((int) newId, fullName, email);
            Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }
}
