package com.example.btl_g03.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_g03.Models.Post;
import com.example.btl_g03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvDescription, tvCategory, tvPostDate, tvStatus,tvSellerEmail;
    private ImageView imgPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        // Khởi tạo các view
        tvTitle = findViewById(R.id.tv_post_detail_title);
        tvDescription = findViewById(R.id.tv_post_detail_description);
        tvCategory = findViewById(R.id.tv_post_detail_category);
        tvPostDate = findViewById(R.id.tv_post_detail_date);
        tvStatus = findViewById(R.id.tv_post_detail_status);
        tvSellerEmail = findViewById(R.id.tv_post_detail_seller_email);
        imgPost = findViewById(R.id.img_post_detail_image);

        findViewById(R.id.btn_logout).setOnClickListener(view -> {
            // Chuyển sang Activity khác (HomeActivity)
            Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();  // Đóng Activity hiện tại nếu cần
        });

        // Lấy postId từ Intent
        String postId = getIntent().getStringExtra("postId");

        if (postId != null && !postId.isEmpty()) {
            loadPostDetail(postId);
        } else {
            Toast.makeText(this, "Post ID is missing", Toast.LENGTH_SHORT).show();
        }

    }

    // Hàm để tải chi tiết bài đăng từ Firestore
    private void loadPostDetail(String postId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference postRef = firestore.collection("product").document(postId);

        postRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Post post = document.toObject(Post.class);

                    if (post != null) {
                        // Hiển thị các thông tin lên màn hình
                        tvTitle.setText(post.getTitle());
                        tvDescription.setText(post.getDescription());
                        tvCategory.setText(post.getCategory());
                        tvPostDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(post.getPostDate()));
                        tvStatus.setText(post.isAvailable() ? "Available" : "Not Available");
                        loadSellerEmail(post.getUserId());

                        // Sử dụng Glide để tải ảnh từ URL nếu có
                        String imageUrl = post.getImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(imgPost);
                        } else {
                            imgPost.setImageResource(R.drawable.ic_image_post);  // Đặt hình ảnh mặc định nếu không có URL ảnh
                        }
                    } else {
                        Toast.makeText(this, "Error: Post object is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load post details", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadSellerEmail(String userId) {
        // Truy vấn Firestore để lấy thông tin người bán dựa trên userId
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("profile").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy email từ tài liệu người bán
                            String email = document.getString("email");
                            tvSellerEmail.setText(email);  // Hiển thị email người bán
                        } else {
                            Log.d("PostDetailActivity", "No such document!");
                        }
                    } else {
                        Log.d("PostDetailActivity", "Get failed with ", task.getException());
                    }
                });
    }
}
