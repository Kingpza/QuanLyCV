package com.example.quanlycv;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.SharedPrefManager;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchPinLock;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        prefManager = new SharedPrefManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        switchPinLock = findViewById(R.id.switchPinLock);
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Button btnExportCSV = findViewById(R.id.btnExportCSV);
        Button btnClearData = findViewById(R.id.btnClearData);

        // Dark Mode State
        switchDarkMode.setChecked(prefManager.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Language Spinner
        String[] languages = {"Tiếng Việt (VI)", "English (EN)"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(langAdapter);
        if ("en".equalsIgnoreCase(prefManager.getLanguage())) {
            spinnerLanguage.setSelection(1);
        } else {
            spinnerLanguage.setSelection(0);
        }
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefManager.setLanguage(position == 1 ? "en" : "vi");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // PIN Lock Switch
        switchPinLock.setChecked(prefManager.isPinEnabled());
        switchPinLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showSetPinDialog();
            } else {
                prefManager.setPinLock(false, "");
                Toast.makeText(this, "Đã tắt khóa ứng dụng!", Toast.LENGTH_SHORT).show();
            }
        });

        btnExportCSV.setOnClickListener(v -> exportDataToCSV());
        btnClearData.setOnClickListener(v -> showClearDataConfirmation());
    }

    private void showSetPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thiết lập mã PIN bảo mật (4 số)");

        final EditText input = new EditText(this);
        input.setHint("Nhập 4 chữ số");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String pin = input.getText().toString().trim();
            boolean isValidPin = (pin.length() == 4);
            if (isValidPin) {
                prefManager.setPinLock(true, pin);
                Toast.makeText(this, "Đã bật khóa PIN thành công!", Toast.LENGTH_SHORT).show();
            } else {
                switchPinLock.setChecked(false);
                Toast.makeText(this, "Mã PIN phải đủ 4 chữ số!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> switchPinLock.setChecked(false));
        builder.show();
    }

    @SuppressWarnings("UnnecessaryCallToStringValueOf")
    private void exportDataToCSV() {
        try {
            List<Task> tasks = dbHelper.getFilteredTasks(0, "", 0, 0);
            File exportDir = new File(getExternalFilesDir(null), "Exports");
            if (!exportDir.exists()) {
                boolean created = exportDir.mkdirs();
                if (!created) {
                    Toast.makeText(this, "Khởi tạo thư mục xuất dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            File file = new File(exportDir, "QuanLyCV_Export.csv");
            FileWriter writer = new FileWriter(file);
            writer.append("ID,Tiêu đề,Mô tả,Ngày,Giờ,Trạng thái,Ưu tiên\n");

            for (Task t : tasks) {
                writer.append(String.valueOf(t.getId())).append(",")
                        .append(t.getTitle()).append(",")
                        .append(t.getDesc() != null ? t.getDesc() : "").append(",")
                        .append(t.getDate()).append(",")
                        .append(t.getTime()).append(",")
                        .append(String.valueOf(t.getStatus())).append(",")
                        .append(String.valueOf(t.getPriority())).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "Đã xuất CSV: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Lỗi khi xuất CSV", e);
            Toast.makeText(this, "Lỗi khi xuất CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearDataConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO: Xóa dữ liệu")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ công việc và khôi phục ứng dụng về trạng thái ban đầu không?")
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    dbHelper.clearAllData();
                    Toast.makeText(this, "Đã xóa toàn bộ dữ liệu!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }
}
