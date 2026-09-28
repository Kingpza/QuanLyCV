package com.example.quanlycv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.utils.SharedPrefManager;

import java.util.Objects;

public class PinLockActivity extends AppCompatActivity {

    private EditText etPinInput;

    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        prefManager = new SharedPrefManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etPinInput = findViewById(R.id.etPinInput);
        Button btnSubmitPin = findViewById(R.id.btnSubmitPin);

        btnSubmitPin.setOnClickListener(v -> {
            String inputPin = etPinInput.getText().toString().trim();
            if (Objects.equals(inputPin, prefManager.getPinCode())) {
                MainActivity.setPinVerified(true);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Mã PIN không đúng!", Toast.LENGTH_SHORT).show();
                etPinInput.setText("");
            }
        });
    }
}
