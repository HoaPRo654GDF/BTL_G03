package com.example.btl_g03.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;


import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.btl_g03.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;




public class HomeActivity extends AppCompatActivity {
    // Khai báo các biến
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId ;
    private ImageView ImUserProfile;
    BottomNavigationView bottomNavigationView;

    //Khởi tạo activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loadFragment(new HomeFragment());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        // Xử lý chuyển fragment khi click biểu tượng tương ứng trên bottom menu
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;

            if (menuItem.getItemId() == R.id.action_home) {
                selectedFragment = new HomeFragment();
            }else if (menuItem.getItemId() == R.id.action_location) {
                selectedFragment = new MapFragment();
            }else if (menuItem.getItemId() == R.id.action_Message) {
                selectedFragment = new NotificationFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {

            userId = currentUser.getUid();

        }
        ImUserProfile = findViewById(R.id.imgUserProfile);

        loadUserData();
        // Thiết lập chuyển đến trang Profile khi nhấn vào ảnh đại diện
        ImageView imgUserProfile = findViewById(R.id.imgUserProfile);
        imgUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Đăng xuất người dùng khi nhấn nút đăng xuất
        findViewById(R.id.btnlogout).setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });
        // xử lý khi bấm nút quay lại
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show a confirmation dialog
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Bạn có muốn thoát ứng dụng không?")
                        .setCancelable(false)
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Thoát ứng dụng
                                finishAffinity(); // Kết thúc toàn bộ activity và thoát ứng dụng
                            }
                        })
                        .setNegativeButton("Không", null) // Đóng hộp thoại nếu chọn "Không"
                        .show();
            }
        });

    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    private void loadUserData() {
        firestore.collection("profile").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imgUrl = document.getString("profileImageUrl");

                            // Gán dữ liệu vào các trường
                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                Glide.with(HomeActivity.this)
                                        .load(imgUrl)// URL ảnh
                                        .transform(new CircleCrop())
                                        .into(ImUserProfile); // profileImageView là ImageView bạn muốn hiển thị ảnh
                            } else {
                                // Nếu không có ảnh, bạn có thể set một ảnh mặc định hoặc không làm gì cả
                                ImUserProfile.setImageResource(R.drawable.defaul_image); // Ví dụ ảnh mặc định
                            }

                        } else {
                            Toast.makeText(HomeActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Không tải được dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


