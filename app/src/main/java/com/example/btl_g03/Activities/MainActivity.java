package com.example.btl_g03.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.btl_g03.R;


import android.content.Intent;

import android.os.Handler;
import android.widget.Button;
import androidx.core.view.WindowCompat;
import androidx.activity.EdgeToEdge;


public class MainActivity extends AppCompatActivity {
//    Button btn_email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        btn_email = findViewById(R.id.btn_email);
//
//
//        btn_email.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//        });

        // Dừng lại trên màn hình chào mừng 3 giây rồi chuyển qua LoginActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000); // 3000 ms = 3 giây

    }
}