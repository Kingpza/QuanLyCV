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
import com.example.quanlycv.utils.SharedPrefManager;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName, etProfileEmail;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        prefManager = new SharedPrefManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        TextView tvProfileTotal = findViewById(R.id.tvProfileTotal);
        TextView tvProfileStreak = findViewById(R.id.tvProfileStreak);

        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        etProfileName.setText(prefManager.getUserName());
        etProfileEmail.setText(prefManager.getUserEmail());

        int total = dbHelper.getTotalCount();
        int streak = dbHelper.getStreakDays();
        tvProfileTotal.setText(getString(R.string.profile_total_tasks_format, total));
        tvProfileStreak.setText(getString(R.string.streak_and_pending_format, streak, 0).split("•")[0].trim());

        btnSaveProfile.setOnClickListener(v -> {
            String newName = etProfileName.getText().toString().trim();
            String newEmail = etProfileEmail.getText().toString().trim();

            boolean isNameEmpty = newName.isEmpty();
            if (isNameEmpty) {
                etProfileName.setError("Vui lòng nhập họ và tên!");
                etProfileName.requestFocus();
                return;
            }

            boolean isEmailEmpty = newEmail.isEmpty();
            if (isEmailEmpty) {
                etProfileEmail.setError("Vui lòng nhập email!");
                etProfileEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etProfileEmail.setError("Email không hợp lệ!");
                etProfileEmail.requestFocus();
                return;
            }

            prefManager.saveUserName(newName);
            prefManager.saveUserEmail(newEmail);
            dbHelper.updateUserProfile(prefManager.getUserId(), newName, newEmail, null);
            Toast.makeText(this, "Đã lưu thông tin cá nhân!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            prefManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
