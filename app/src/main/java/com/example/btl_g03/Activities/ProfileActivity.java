package com.example.btl_g03.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btl_g03.DatabaseHelper;
import com.example.btl_g03.Models.User;
import com.example.btl_g03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEmail;
    private EditText edtFullName, edtPhoneNumber, edtAddress;
    private Button btnSave;
    private ImageView imgProfile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private Uri selectedImageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            email = currentUser.getEmail();
            userId = currentUser.getUid();
            password = currentUser.getUid();
        }

        // Khởi tạo các thành phần UI
        txtEmail = findViewById(R.id.txt_email);
        edtFullName = findViewById(R.id.edt_full_name);
        edtPhoneNumber = findViewById(R.id.edt_phone_number);
        edtAddress = findViewById(R.id.edt_address);
        btnSave = findViewById(R.id.btn_save);
        imgProfile = findViewById(R.id.img_profile);

        imgProfile.setOnClickListener(v -> showImageOptionsDialog());

        // Tải dữ liệu người dùng
        loadUserData();

        // Xử lý sự kiện khi nhấn nút lưu
        btnSave.setOnClickListener(v -> saveUserData());
        findViewById(R.id.btnlogout).setOnClickListener(view -> {
            // Chuyển sang Activity khác (HomeActivity)
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();  // Đóng Activity hiện tại nếu cần
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imgProfile.setImageBitmap(imageBitmap);
                selectedImageUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                imgProfile.setImageURI(selectedImageUri);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call super method

        if (requestCode == 100 && grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (!cameraAccepted || !storageAccepted) {
                Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }
    private void showImageOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh");
        builder.setItems(new CharSequence[]{"Từ Camera", "Từ ảnh"}, (dialog, which) -> {
            switch (which) {
                case 0: // Camera
                    openCamera();
                    break;
                case 1: // Gallery
                    openGallery();
                    break;
            }
        });
        builder.show();
    }
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            // Tạo đường dẫn lưu ảnh vào bộ nhớ trong
            File storageDir = new File(getFilesDir(), "images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(storageDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Trả về đường dẫn tuyệt đối của ảnh đã lưu
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void loadUserData() {
        firestore.collection("profile").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String phoneNumber = document.getString("phoneNumber");
                            String address = document.getString("address");
                            String imgUrl = document.getString("profileImageUrl");

                            // Gán dữ liệu vào các trường


                            txtEmail.setText(email); // Email không thể chỉnh sửa
                            edtFullName.setText(fullName);
                            edtPhoneNumber.setText(phoneNumber);
                            edtAddress.setText(address);
                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load(imgUrl) // URL ảnh
                                        .into(imgProfile); // profileImageView là ImageView bạn muốn hiển thị ảnh
                            } else {
                                // Nếu không có ảnh, bạn có thể set một ảnh mặc định hoặc không làm gì cả
                                imgProfile.setImageResource(R.drawable.defaul_image); // Ví dụ ảnh mặc định
                            }

                        } else {
                            Toast.makeText(ProfileActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Không tải được dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData() {
        String fullName = edtFullName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String address = edtAddress.getText().toString();

        if (!fullName.isEmpty() && !phoneNumber.isEmpty() && !address.isEmpty()) {
            if (selectedImageUri != null) {
                Log.d("HomeActivity", "Selected Image URI: " + selectedImageUri);

                // Lưu ảnh vào bộ nhớ trong hoặc ngoài (ở đây giả sử bạn lưu vào bộ nhớ trong)
                String imagePath = saveImageToInternalStorage(selectedImageUri);

                // Lưu sản phẩm vào SQLite
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                dbHelper.addUser(userId, email, phoneNumber, address, fullName, imagePath); // Lưu vào SQLite

                DocumentReference userRef = firestore.collection("profile").document(userId);

                // Kiểm tra tài liệu người dùng có tồn tại hay không
                userRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        User user = new User(userId, auth.getCurrentUser().getEmail(), password, fullName, phoneNumber, address, imagePath);

                        if (document.exists()) {
                            // Nếu tài liệu đã tồn tại, cập nhật dữ liệu
                            userRef.set(user) // .set() sẽ ghi đè tài liệu, hoặc tạo mới nếu không có
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ProfileActivity.this, "Cập nhật dữ liệu thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProfileActivity.this, "Lỗi khi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
                                        Log.d("ProfileActivity", "Error updating document", e);
                                    });
                        } else {
                            // Nếu tài liệu chưa tồn tại, tạo mới tài liệu
                            userRef.set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ProfileActivity.this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProfileActivity.this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                                        Log.d("ProfileActivity", "Error adding document", e);
                                    });
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ProfileActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }

}

