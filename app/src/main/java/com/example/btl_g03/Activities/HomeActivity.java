package com.example.btl_g03.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_g03.Adapters.PostAdapter;

import android.Manifest;

import com.example.btl_g03.DatabaseHelper;
import com.example.btl_g03.Models.Post;
import com.example.btl_g03.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postlist;
    private FloatingActionButton btn_add;
    private Button btn_them, btn_huy;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private  ImageView img_post_image;
    private TextInputEditText edt_post_title, edt_post_description, edt_post_category, edt_post_date,edt_post_status;
    private String userId ;
    private Uri selectedImageUri;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {

            userId = currentUser.getUid();

        }

        // Setup RecyclerView and FloatingActionButton
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btn_add = findViewById(R.id.btn_add);

        // Initialize list and adapter
        postlist = new ArrayList<>();
        postAdapter = new PostAdapter(postlist,this);
        recyclerView.setAdapter(postAdapter);

        // Load existing user data
        docDulieu();
        calendar = Calendar.getInstance();

        btn_add.setOnClickListener(v -> showAddOrUpdateDialog());
        ImageView imgUserProfile = findViewById(R.id.imgUserProfile); // icon hình người trong layout HomeActivity
        imgUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnlogout).setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });
        // xử lý khi bấm nút quay lại từ trang home
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                img_post_image.setImageBitmap(imageBitmap);
                selectedImageUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                img_post_image.setImageURI(selectedImageUri);
            }
        }
    }



    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
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



    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Cập nhật Calendar với ngày đã chọn
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Định dạng ngày và hiển thị trong edt_post_date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    edt_post_date.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showAddOrUpdateDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.item_post_product, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        // Find and initialize views
        btn_them = view.findViewById(R.id.btn_them);
        btn_huy = view.findViewById(R.id.btn_huy);
        edt_post_title = view.findViewById(R.id.edt_post_title);
        edt_post_category = view.findViewById(R.id.edt_post_category);
        edt_post_date = view.findViewById(R.id.edt_post_date);
        edt_post_description = view.findViewById(R.id.edt_post_description);
        edt_post_status = view.findViewById(R.id.edt_post_status);
        img_post_image = view.findViewById(R.id.img_post_image);

        // "Add" button logic
        btn_them.setOnClickListener(v1 -> ghiDulieu(alertDialog));
        img_post_image.setOnClickListener(v -> showImageOptionsDialog());
        // "Cancel" button logic
        btn_huy.setOnClickListener(v1 -> alertDialog.dismiss());

        alertDialog.show();
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


    private void ghiDulieu(AlertDialog alertDialog) {
        String title = edt_post_title.getText().toString();
        String category = edt_post_category.getText().toString();
        String dateString = edt_post_date.getText().toString();
        String description = edt_post_description.getText().toString();
        String status = edt_post_status.getText().toString();

        Log.d("HomeActivity", "Title: " + title);
        Log.d("HomeActivity", "Category: " + category);
        Log.d("HomeActivity", "Date: " + dateString);
        Log.d("HomeActivity", "Description: " + description);
        Log.d("HomeActivity", "Status: " + status);

        boolean isAvailable = true;

        // Định dạng ngày mà bạn mong muốn (dd/MM/yyyy)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = null;
        try {
            // Chuyển đổi chuỗi thành kiểu Date
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // Xử lý nếu chuỗi ngày không đúng định dạng
            Toast.makeText(this, "Vui lòng nhập đúng định dạng ngày (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return;
        }

        final Date finalDate = date;

        // Kiểm tra nếu finalDate là null
        if (finalDate == null) {
            Toast.makeText(this, "Ngày không hợp lệ, vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra các trường thông tin có rỗng không
        if (!title.isEmpty() && !category.isEmpty() && date != null && !description.isEmpty() && !status.isEmpty()) {
            if (selectedImageUri != null) {
                Log.d("HomeActivity", "Selected Image URI: " + selectedImageUri);

                // Lưu ảnh vào bộ nhớ trong hoặc ngoài (ở đây giả sử bạn lưu vào bộ nhớ trong)
                String imagePath = saveImageToInternalStorage(selectedImageUri);

                // Lưu sản phẩm vào SQLite
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                dbHelper.addProduct(title, description, category, imagePath); // Lưu vào SQLite

                // Tạo tham chiếu đến Firestore để lưu thông tin sản phẩm (chỉ thông tin không bao gồm ảnh)
                DocumentReference postRef = firestore.collection("product").document();
                String postId = postRef.getId(); // Lấy ID tự động của tài liệu

                // Tạo đối tượng Post và lưu vào Firestore
                Post post = new Post(postId, userId, title, description, category, imagePath, finalDate, isAvailable);
                postRef.set(post)
                        .addOnSuccessListener(aVoid -> {
                            alertDialog.dismiss();
                            docDulieu();
                            Toast.makeText(HomeActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(HomeActivity.this, "Lỗi khi thêm dữ liệu", Toast.LENGTH_SHORT).show();
                            Log.d("HomeActivity", "Error adding document", e);
                        });
            } else {
                Toast.makeText(HomeActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(HomeActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức lưu ảnh vào bộ nhớ trong (hoặc ngoài)
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


    private void docDulieu() {
        firestore.collection("product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postlist.clear(); // Clear old data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post postData = document.toObject(Post.class);
                            postlist.add(postData);
                        }
                        postAdapter.notifyDataSetChanged(); // Refresh adapter with new data
                    } else {
                        Log.d("HomeActivity", "Error getting documents: ", task.getException());
                    }
                });
    }


}


