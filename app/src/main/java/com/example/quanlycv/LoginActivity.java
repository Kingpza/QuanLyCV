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

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginAccount, etLoginPassword;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        prefManager = new SharedPrefManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etLoginAccount = findViewById(R.id.etLoginAccount);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvLinkRegister = findViewById(R.id.tvLinkRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvLinkRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String account = etLoginAccount.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.checkLogin(account, password);
        if (user != null) {
            prefManager.saveUserLogin(user.getId(), user.getFullName(), user.getEmail());
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, R.string.err_login_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
