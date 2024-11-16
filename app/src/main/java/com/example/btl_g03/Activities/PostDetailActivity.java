    package com.example.btl_g03.Activities;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    import com.example.btl_g03.Models.Notification;
    import com.example.btl_g03.Models.Post;
    import com.example.btl_g03.Models.Request;
    import com.example.btl_g03.R;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import com.bumptech.glide.Glide;

    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Locale;
    import java.util.UUID;

    public class PostDetailActivity extends AppCompatActivity {
        private TextView tvTitle, tvDescription, tvCategory, tvPostDate, tvStatus,tvSellerEmail,tvPostType;
        private ImageView imgPost;
        private Button btnRequestItem;
        private String currentUserId ;
        private Button btnConfirmRequest;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_post_detail);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

            if (currentUserId == null) {
                // Người dùng chưa đăng nhập, chuyển hướng đến màn hình đăng nhập
                Intent intent = new Intent(PostDetailActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();  // Đóng màn hình hiện tại
                return;  // Dừng các hoạt động còn lại của onCreate()
            }




            // Khởi tạo các view
            tvTitle = findViewById(R.id.tv_post_detail_title);
            tvDescription = findViewById(R.id.tv_post_detail_description);
            tvCategory = findViewById(R.id.tv_post_detail_category);
            tvPostDate = findViewById(R.id.tv_post_detail_date);
            tvStatus = findViewById(R.id.tv_post_detail_status);
            tvSellerEmail = findViewById(R.id.tv_post_detail_seller_email);
            tvPostType = findViewById(R.id.tv_post_detail_type);
            imgPost = findViewById(R.id.img_post_detail_image);
            btnRequestItem = findViewById(R.id.btn_request_item);
            btnConfirmRequest = findViewById(R.id.btn_confirm_request);

            String fromNotification = getIntent().getStringExtra("fromNotification");

            if ("true".equals(fromNotification)) {
                btnConfirmRequest.setVisibility(View.VISIBLE);
                btnConfirmRequest.setOnClickListener(v -> {confirmRequest();
                });
            } else {
                btnConfirmRequest.setVisibility(View.GONE);
            }

            findViewById(R.id.btn_logout).setOnClickListener(view -> {
                // Chuyển sang Activity khác (HomeActivity)
                Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();  // Đóng Activity hiện tại nếu cần
            });

            btnRequestItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ensure postId is available here
    //                String postId = getIntent().getStringExtra("postId");
    //                if (postId != null && !postId.isEmpty()) {
    //                    sendNotificationToSeller(postId);  // Call sendNotificationToSeller with postId
    //                } else {
    //                    Toast.makeText(PostDetailActivity.this, "Post ID is missing", Toast.LENGTH_SHORT).show();
    //                }

                    // Show the request dialog
                    showRequestDialog();
                }
            });

            // Lấy postId từ Intent
            String postId = getIntent().getStringExtra("postId");

            if (postId != null && !postId.isEmpty()) {
                loadPostDetail(postId);
            } else {
                Toast.makeText(this, "Post ID is missing", Toast.LENGTH_SHORT).show();
            }

        }

        private void confirmRequest() {
            String postId = getIntent().getStringExtra("postId");
            String requestId = getIntent().getStringExtra("requestId");
            if (requestId != null) {
                DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests").child(requestId);
                requestRef.child("status").setValue("confirmed")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PostDetailActivity.this, "Yêu cầu đã được xác nhận.", Toast.LENGTH_SHORT).show();
                                btnConfirmRequest.setVisibility(View.GONE);  // Ẩn nút sau khi xác nhận
                                sendNotificationToReceiver(postId, requestId);
                            } else {
                                Toast.makeText(PostDetailActivity.this, "Lỗi khi xác nhận yêu cầu.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Không tìm thấy yêu cầu để xác nhận.", Toast.LENGTH_SHORT).show();
            }
        }

        private void showRequestDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Yêu cầu nhận sản phẩm");

            // Tạo EditText để người dùng nhập thông điệp
            final EditText input = new EditText(this);
            input.setHint("Nhập lời nhắn tới người đăng...");
            builder.setView(input);

            builder.setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
                String message = input.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Tạo yêu cầu
                    sendRequest(message);

                } else {
                    Toast.makeText(this, "Vui lòng nhập lời nhắn.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void sendRequest(String message) {
            // Lấy postId và requesterId từ dữ liệu bài đăng và người dùng hiện tại
            String postId = getIntent().getStringExtra("postId");
            String requesterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String requestId = UUID.randomUUID().toString();
            Date requestDate = new Date();

            // Tạo đối tượng Request
            Request request = new Request(requestId, postId, requesterId, requestDate, message);

            // Lưu yêu cầu vào Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("requests");
            databaseReference.child(requestId).setValue(request).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Yêu cầu đã được gửi.", Toast.LENGTH_SHORT).show();
                     // Lấy ID của người bán từ bài đăng
                    sendNotificationToSeller(postId, requestId,message);

                } else {
                    Toast.makeText(this, "Lỗi khi gửi yêu cầu.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sendNotificationToSeller(String postId,String requestId,String message) {
            // Lấy thông tin sellerId từ Firestore
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            DocumentReference postRef = firestore.collection("product").document(postId);

            // Truy vấn thông tin người bán từ Firestore
            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        // Lấy sellerId từ dữ liệu của bài đăng
                        String sellerId = documentSnapshot.getString("userId"); // Thay "sellerId" bằng trường trong Firestore lưu sellerId

                        // Kiểm tra nếu sellerId không null hoặc rỗng
                        if (sellerId == null || sellerId.isEmpty()) {
                            Toast.makeText(PostDetailActivity.this, "ID người bán không hợp lệ.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Tạo ID thông báo và nội dung thông báo
                        String notificationId = UUID.randomUUID().toString();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserEmail = user.getEmail();
                        String content = "Có một yêu cầu nhận sản phẩm mới cho bài đăng của bạn từ " + currentUserEmail + " với lời nhắn: "+ message+". Vui lòng xác nhận.";

                        Date createdDate = new Date();


                        // Tạo đối tượng Notification
                        Notification notification = new Notification(notificationId, sellerId, content, createdDate,postId,requestId);

                        // Gửi thông báo vào Firebase Realtime Database
                        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications");
                        notificationRef.child(notificationId).setValue(notification)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Thông báo đã được gửi thành công
                                        Toast.makeText(PostDetailActivity.this, "Thông báo đã được gửi cho người đăng.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Xử lý lỗi khi gửi thông báo
                                        Toast.makeText(PostDetailActivity.this, "Lỗi khi gửi thông báo.", Toast.LENGTH_SHORT).show();
                                        Log.e("sendNotification", "Error sending notification", task1.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Không tìm thấy bài đăng.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this, "Lỗi khi truy vấn dữ liệu bài đăng.", Toast.LENGTH_SHORT).show();
                    Log.e("sendNotification", "Error fetching post data", task.getException());
                }
            });
        }
        private void sendNotificationToReceiver(String postId, String requestId) {
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests").child(requestId);

            requestRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Lấy thông tin từ request
                    Request request = task.getResult().getValue(Request.class);
                    if (request != null) {
                        String receiverId = request.getRequesterId();  // Giả sử bạn lưu receiverId trong đối tượng Request

                        if (receiverId != null && !receiverId.isEmpty()) {
                            // Tạo thông báo cho người nhận
                            String notificationId = UUID.randomUUID().toString();
                            String content = "Bạn đã nhận yêu cầu xác nhận sản phẩm từ người đăng, hãy đến vị trí của người đăng để nhận";
                            Date createdDate = new Date();

                            // Tạo đối tượng Notification
                            Notification notification = new Notification(notificationId, receiverId, content, createdDate, postId, requestId);

                            // Lưu thông báo vào Firebase Realtime Database
                            DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications");
                            notificationRef.child(notificationId).setValue(notification).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(PostDetailActivity.this, "Thông báo đã được gửi cho người nhận.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PostDetailActivity.this, "Lỗi khi gửi thông báo.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(PostDetailActivity.this, "Không tìm thấy người nhận.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Không tìm thấy yêu cầu.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this, "Lỗi khi truy vấn dữ liệu yêu cầu.", Toast.LENGTH_SHORT).show();
                }
            });
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
                            tvStatus.setText(post.isAvailable() ? "Còn hàng" : "Hết hàng");
                            tvPostType.setText(post.getPostType().toString());
                            loadSellerEmail(post.getUserId());
                            if (currentUserId != null && post.getUserId() != null) {
                                if (currentUserId.equals(post.getUserId())) {
                                    // Ẩn nút yêu cầu nếu là người đăng bài
                                    btnRequestItem.setVisibility(View.GONE);
                                } else {
                                    // Hiển thị nút yêu cầu nếu không phải là người đăng bài
                                    btnRequestItem.setVisibility(View.VISIBLE);
                                }
                            } else {
                                // Có thể thông báo người dùng chưa đăng nhập hoặc có vấn đề trong việc lấy thông tin người dùng
                                Toast.makeText(PostDetailActivity.this, "Lỗi khi xác nhận người dùng", Toast.LENGTH_SHORT).show();
                            }

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
