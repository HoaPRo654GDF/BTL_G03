package com.example.btl_g03.Activities;

import static android.app.Activity.RESULT_OK;



import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_g03.Adapters.PostAdapter;
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


public class HomeFragment extends Fragment {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postlist;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int CAMERA_REQUEST_CODE = 100;
    private FloatingActionButton btn_add;
    private Button btn_them, btn_huy;
    private ImageView img_post_image;
    private TextInputEditText edt_post_title, edt_post_description, edt_post_category, edt_post_date, edt_post_status;
    private Uri selectedImageUri;
    private Calendar calendar;
    private String userId ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {

            userId = currentUser.getUid();

        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        btn_add = view.findViewById(R.id.btn_add);
        postlist = new ArrayList<>();
        postAdapter = new PostAdapter(postlist, requireContext());
        recyclerView.setAdapter(postAdapter);

        docDulieu();

        calendar = Calendar.getInstance();
        btn_add.setOnClickListener(v -> showAddOrUpdateDialog());

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                img_post_image.setImageURI(selectedImageUri); // Hiển thị ảnh từ Camera
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                img_post_image.setImageBitmap(imageBitmap); // Hiển thị ảnh chụp
                selectedImageUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                img_post_image.setImageURI(selectedImageUri); // Hiển thị ảnh từ gallery
            }
        }
    }
    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    private File createImageFile() throws IOException {
        // Đặt tên cho file ảnh với timestamp để tránh trùng lặp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Tạo file ảnh
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Lưu đường dẫn file để sử dụng sau
        return image;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                selectedImageUri = FileProvider.getUriForFile(getContext(), "com.example.btl_g03.fileprovider", photoFile);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.item_post_product, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        // Tìm các view trong dialog
        btn_them = dialogView.findViewById(R.id.btn_them);
        btn_huy = dialogView.findViewById(R.id.btn_huy);
        edt_post_title = dialogView.findViewById(R.id.edt_post_title);
        edt_post_category = dialogView.findViewById(R.id.edt_post_category);
        edt_post_date = dialogView.findViewById(R.id.edt_post_date);
        edt_post_description = dialogView.findViewById(R.id.edt_post_description);
        edt_post_status = dialogView.findViewById(R.id.edt_post_status);
        img_post_image = dialogView.findViewById(R.id.img_post_image);

        // Logic cho nút thêm
        btn_them.setOnClickListener(v1 -> ghiDulieu(alertDialog));

        edt_post_date.setOnClickListener(v -> showDatePickerDialog());

        img_post_image.setOnClickListener(v -> {
            requestPermissions();
            showImageOptionsDialog();
        });

        // Logic cho nút hủy
        btn_huy.setOnClickListener(v1 -> alertDialog.dismiss());

        alertDialog.show();
    }
    private String saveImageToInternalStorage(Uri imageUri) {
        try {

            // Tạo đường dẫn lưu ảnh vào bộ nhớ trong
            File storageDir = new File(requireContext().getFilesDir(), "images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(storageDir, fileName);

            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
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
            Toast.makeText(getContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    private void ghiDulieu(AlertDialog alertDialog) {
        // Lưu dữ liệu như trước, thay vì gọi HomeActivity, sử dụng getContext() cho các thao tác cần thiết.
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
            Toast.makeText(getContext(), "Vui lòng nhập đúng định dạng ngày (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return;
        }

        final Date finalDate = date;

        // Kiểm tra nếu finalDate là null
        if (finalDate == null) {
            Toast.makeText(getContext(), "Ngày không hợp lệ, vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra các trường thông tin có rỗng không
        if (!title.isEmpty() && !category.isEmpty() && date != null && !description.isEmpty() && !status.isEmpty()) {
            if (selectedImageUri != null) {
                Log.d("HomeActivity", "Selected Image URI: " + selectedImageUri);

                // Lưu ảnh vào bộ nhớ trong hoặc ngoài (ở đây giả sử bạn lưu vào bộ nhớ trong)
                String imagePath = saveImageToInternalStorage(selectedImageUri);

                // Lưu sản phẩm vào SQLite
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
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
                            Toast.makeText(getContext(), "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Lỗi khi thêm dữ liệu", Toast.LENGTH_SHORT).show();
                            Log.d("HomeActivity", "Error adding document", e);
                        });
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
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
                        postAdapter.notifyDataSetChanged();
                    } else {
                            Log.d("HomeFragment", "Lỗi lấy dữ liệu: ", task.getException());
                    }
                });
    }




    // Các phương thức khác như docDulieu(), showAddOrUpdateDialog(), requestPermissions(), openCamera(), openGallery() sẽ được chuyển vào đây.
}

