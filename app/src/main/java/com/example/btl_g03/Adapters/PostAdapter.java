package com.example.btl_g03.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Glide để tải ảnh từ URL
import com.example.btl_g03.Models.Post;
import com.example.btl_g03.R;
import com.example.btl_g03.Activities.PostDetailActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;
    private String currentUserId;

    public PostAdapter(List<Post> postList, Context context,String currentUserId) {
        this.postList = postList;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        String title = post.getTitle();
        String imageUrl = post.getImageUrl(); // Lấy URL ảnh từ Firestore

        holder.tvTitle.setText(title);

        // Kiểm tra URL ảnh có hợp lệ không
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)  // Tải ảnh từ URL
                    .placeholder(R.drawable.ic_image_post)  // Ảnh mặc định khi chưa tải xong
                    .into(holder.imgPost);  // Đặt ảnh vào ImageView
        } else {
            holder.imgPost.setImageResource(R.drawable.ic_image_post);  // Nếu không có URL, dùng ảnh mặc định
        }

        if (post.getUserId().equals(currentUserId)) {
            holder.btnDeletePost.setVisibility(View.VISIBLE);
            holder.btnDeletePost.setOnClickListener(v -> {
                deletePost(post.getPostId(), position);
            });
        } else {
            holder.btnDeletePost.setVisibility(View.GONE);
        }

        // Thiết lập sự kiện click vào item
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId());  // Gửi ID của bài đăng
            context.startActivity(intent);
        });
    }
    private void deletePost(String postId, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        DatabaseReference notificationsRef2 = FirebaseDatabase.getInstance().getReference("posts");

        // Xóa bài đăng từ Firestore
        firestore.collection("product").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa bài đăng thành công, giờ xóa thông báo trong Realtime Database
                    notificationsRef.orderByChild("postId").equalTo(postId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Xóa thông báo có postId tương ứng
                                        snapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    // Thông báo thành công khi xóa thông báo
                                                    //Toast.makeText(context, "Xóa thông báo thành công", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Thêm thông báo lỗi khi không xóa được thông báo
                                                    //Toast.makeText(context, "Không thể xóa thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("DeletePost", "deletePost:onCancelled", databaseError.toException());
                                }
                            });
                    // Xóa bài đăng thành công, giờ xóa vị trí trong Realtime Database
                    notificationsRef2.orderByChild("postId").equalTo(postId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Xóa vị trí có postId tương ứng
                                        snapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    // Thông báo thành công khi xóa thông báo
                                                    //Toast.makeText(context, "Xóa thông báo thành công", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Thêm thông báo lỗi khi không xóa được thông báo
                                                    //Toast.makeText(context, "Không thể xóa thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("DeletePost", "deletePost:onCancelled", databaseError.toException());
                                }
                            });


                    // Xóa bài đăng khỏi danh sách
                    postList.remove(position);
                    notifyItemRemoved(position); // Cập nhật RecyclerView
                    Toast.makeText(context, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Thông báo khi không thể xóa bài đăng
                    Toast.makeText(context, "Lỗi khi xóa bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    public void updatePostList(List<Post> newPostList) {
//        this.postList = newPostList;
//        notifyDataSetChanged(); // Cập nhật lại RecyclerView
//    }



    @Override
    public int getItemCount() {
        return postList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imgPost;
        Button btnViewDetails;
        Button btnDeletePost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            imgPost = itemView.findViewById(R.id.img_post_image);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnDeletePost = itemView.findViewById(R.id.btn_delete_post);
        }
    }
}
