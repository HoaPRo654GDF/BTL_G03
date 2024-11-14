package com.example.btl_g03.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
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
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEmail;
    private EditText edtFullName, edtPhoneNumber, edtAddress;
    private Button btnSave;
    private ImageView imgProfile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private Uri selectedImageUri;
    private static final int CAMERA_REQUEST_CODE = 100;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId, email, password;
    private  String profileImageUrl;

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

        imgProfile.setOnClickListener(v ->{
                requestPermissions();
                showImageOptionsDialog();
        });

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
            if (requestCode == CAMERA_REQUEST_CODE ) {

                imgProfile.setImageURI(selectedImageUri);// Hiển thị ảnh từ Camera
            }
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

    private boolean checkPermissions() {

        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    // Hàm yêu cầu cấp quyền camera và bộ nhớ
    private void requestPermissions() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
    private File createImageFile() throws IOException {
        // Đặt tên cho file ảnh với timestamp để tránh trùng lặp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Tạo file ảnh
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Lưu đường dẫn file để sử dụng sau
        return image;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // Tạo file để lưu ảnh
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Xử lý lỗi khi tạo file
                ex.printStackTrace();
            }

            if (photoFile != null) {
                selectedImageUri = FileProvider.getUriForFile(this, "com.example.btl_g03.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
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
                                        .load(imgUrl)// URL ảnh
                                        .transform(new CircleCrop())
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


//    private void saveUserData() {
//        String fullName = edtFullName.getText().toString();
//        String phoneNumber = edtPhoneNumber.getText().toString();
//        String address = edtAddress.getText().toString();
//
//        if (!fullName.isEmpty() && !phoneNumber.isEmpty() && !address.isEmpty()) {
//            if (selectedImageUri != null) {
//                Log.d("HomeActivity", "Selected Image URI: " + selectedImageUri);
//
//                // Lưu ảnh vào bộ nhớ trong hoặc ngoài (ở đây giả sử bạn lưu vào bộ nhớ trong)
//                String profileimageurl = saveImageToInternalStorage(selectedImageUri);
//
//                // Lưu sản phẩm vào SQLite
//                DatabaseHelper dbHelper = new DatabaseHelper(this);
//                dbHelper.addUser(userId, email, phoneNumber, address, fullName, profileimageurl); // Lưu vào SQLite
//
//                DocumentReference userRef = firestore.collection("profile").document(userId);
//
//                // Kiểm tra tài liệu người dùng có tồn tại hay không
//                userRef.get().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        User user = new User(userId, auth.getCurrentUser().getEmail(), password, fullName, phoneNumber, address, profileimageurl);
//
//                        if (document.exists()) {
//                            // Nếu tài liệu đã tồn tại, cập nhật dữ liệu
//                            userRef.set(user) // .set() sẽ ghi đè tài liệu, hoặc tạo mới nếu không có
//                                    .addOnSuccessListener(aVoid -> {
//                                        Toast.makeText(ProfileActivity.this, "Cập nhật dữ liệu thành công", Toast.LENGTH_SHORT).show();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(ProfileActivity.this, "Lỗi khi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
//                                        Log.d("ProfileActivity", "Error updating document", e);
//                                    });
//                        } else {
//                            // Nếu tài liệu chưa tồn tại, tạo mới tài liệu
//                            userRef.set(user)
//                                    .addOnSuccessListener(aVoid -> {
//                                        Toast.makeText(ProfileActivity.this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(ProfileActivity.this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
//                                        Log.d("ProfileActivity", "Error adding document", e);
//                                    });
//                        }
//                    } else {
//                        Toast.makeText(ProfileActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } else {
//                Toast.makeText(ProfileActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void saveUserData() {
        String fullName = edtFullName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String address = edtAddress.getText().toString();

        if (!fullName.isEmpty() && !phoneNumber.isEmpty() && !address.isEmpty()) {
            // Kiểm tra nếu người dùng chọn ảnh


            // Nếu có ảnh mới được chọn
            if (selectedImageUri != null) {
                Log.d("HomeActivity", "Selected Image URI: " + selectedImageUri);
                profileImageUrl = saveImageToInternalStorage(selectedImageUri);
            } else {
                // Nếu không có ảnh mới, giữ lại ảnh cũ hoặc không thay đổi ảnh
                profileImageUrl = getCurrentProfileImageUrl(); // Bạn cần tạo hàm này để lấy ảnh cũ từ Firestore
            }

            // Lưu thông tin vào SQLite
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.addUser(userId, email, phoneNumber, address, fullName, profileImageUrl);

            DocumentReference userRef = firestore.collection("profile").document(userId);

            // Kiểm tra tài liệu người dùng có tồn tại hay không
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = new User(userId, auth.getCurrentUser().getEmail(), password, fullName, phoneNumber, address, profileImageUrl);

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
            Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentProfileImageUrl() {
        // Tạo tham chiếu đến Firestore để lấy ảnh cũ
        DocumentReference userRef = firestore.collection("profile").document(userId);
        final String[] profileImageUrl = new String[1];

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    profileImageUrl[0] = document.getString("profileImageUrl");
                }
            } else {
                Log.d("ProfileActivity", "Error getting document", task.getException());
            }
        });

        return profileImageUrl[0];
    }

}

