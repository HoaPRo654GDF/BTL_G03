package com.example.btl_g03.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.provider.Settings;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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




public class HomeActivity extends AppCompatActivity implements SensorEventListener {
    // Các biến cho cảm biến ánh sáng
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView lightValueText;
    private FrameLayout topFrame;

    private static final int PERMISSION_CODE = 1234;
    private WindowManager.LayoutParams layoutParams;
    private float currentBrightness = 1.0f;
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

        layoutParams = getWindow().getAttributes();
        checkBrightnessPermission();
        // Khởi tạo sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Ánh xạ view
        lightValueText = findViewById(R.id.lightValue);
        topFrame = findViewById(R.id.topFrame);

        // Kiểm tra xem thiết bị có cảm biến ánh sáng không
        if (lightSensor == null) {
            Toast.makeText(this, "Thiết bị không có cảm biến ánh sáng!", Toast.LENGTH_SHORT).show();
        }


        loadFragment(new HomeFragment());

        String postId = getIntent().getStringExtra("postId");

        if (postId != null) {
            // Chuyển đến MapFragment và truyền postId
            Bundle bundle = new Bundle();
            bundle.putString("postId", postId);

            // Khởi tạo MapFragment
            PostLocationFragment  postLocationFragment  = new PostLocationFragment();
            postLocationFragment.setArguments(bundle);  // Truyền dữ liệu vào Fragment

            // Thực hiện FragmentTransaction để thay thế Fragment hiện tại bằng MapFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, postLocationFragment)  // `R.id.fragment_container` là container chứa các Fragment trong layout của bạn
                    .addToBackStack(null)  // Thêm vào back stack nếu bạn muốn người dùng có thể quay lại Fragment trước đó
                    .commit();
        } else {
//            Toast.makeText(HomeActivity.this, "Post ID is missing", Toast.LENGTH_SHORT).show();
        }


        bottomNavigationView = findViewById(R.id.bottom_nav);
        // Xử lý chuyển fragment khi click biểu tượng tương ứng trên bottom menu
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;

            if (menuItem.getItemId() == R.id.action_home) {
                selectedFragment = new HomeFragment();
            }else if (menuItem.getItemId() == R.id.action_location) {
                
                    selectedFragment = new MapFragment();
            }else if (menuItem.getItemId() == R.id.action_Notification) {
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
    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void checkBrightnessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_CODE);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Khôi phục độ sáng về mức mặc định
        adjustScreenBrightness(1.0f);
    }

    private void adjustScreenBrightness(float brightness) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                if (Math.abs(currentBrightness - brightness) > 0.1f) {
                    currentBrightness = brightness;
                    layoutParams.screenBrightness = brightness;
                    getWindow().setAttributes(layoutParams);

                    // Lưu giá trị độ sáng vào system settings
                    int brightnessInt = (int) (brightness * 255);
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, brightnessInt);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(getApplicationContext())) {
                    Toast.makeText(this, "Đã được cấp quyền điều chỉnh độ sáng",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Chưa được cấp quyền điều chỉnh độ sáng",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightValue = event.values[0];
            lightValueText.setText(String.format("%.1f lux", lightValue));

            if (lightValue < 10) { // Môi trường tối
                adjustScreenBrightness(0.2f); // Giảm độ sáng xuống 20%
                topFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_purple));
                lightValueText.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else if (lightValue < 100) { // Ánh sáng yếu
                adjustScreenBrightness(0.5f); // Độ sáng 50%
                topFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.medium_purple));
                lightValueText.setTextColor(ContextCompat.getColor(this, R.color.black));
            } else { // Ánh sáng đủ
                adjustScreenBrightness(1.0f); // Độ sáng tối đa
                topFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_200));
                lightValueText.setTextColor(ContextCompat.getColor(this, R.color.black));
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý gì khi độ chính xác thay đổi
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
                                        .placeholder(R.drawable.ic_user)
                                        .transform(new CircleCrop())
                                        .into(ImUserProfile); // profileImageView là ImageView bạn muốn hiển thị ảnh
                            } else {

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


