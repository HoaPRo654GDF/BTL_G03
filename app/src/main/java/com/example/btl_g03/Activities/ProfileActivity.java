package com.example.btl_g03.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_g03.Models.User;
import com.example.btl_g03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEmail;
    private EditText edtFullName, edtPhoneNumber, edtAddress;
    private Button btnSave;
    private ImageView imgProfile;

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

        // Tải dữ liệu người dùng
        loadUserData();

        // Xử lý sự kiện khi nhấn nút lưu
        btnSave.setOnClickListener(v -> saveUserData());
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

                            // Gán dữ liệu vào các trường
                            txtEmail.setText(email); // Email không thể chỉnh sửa
                            edtFullName.setText(fullName);
                            edtPhoneNumber.setText(phoneNumber);
                            edtAddress.setText(address);
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
            DocumentReference userRef = firestore.collection("profile").document(userId);

            // Kiểm tra tài liệu người dùng có tồn tại hay không
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = new User(userId, auth.getCurrentUser().getEmail(),password , fullName, phoneNumber, address);

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
                    Toast.makeText(ProfileActivity.this, "Lỗi khi kiểm tra dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }

}
