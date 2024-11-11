package com.example.btl_g03.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Glide để tải ảnh từ URL
import com.example.btl_g03.Models.Post;
import com.example.btl_g03.R;
import com.example.btl_g03.Activities.PostDetailActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
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
                    .placeholder(R.drawable.defaul_image)  // Ảnh mặc định khi chưa tải xong
                    .into(holder.imgPost);  // Đặt ảnh vào ImageView
        } else {
            holder.imgPost.setImageResource(R.drawable.defaul_image);  // Nếu không có URL, dùng ảnh mặc định
        }

        // Thiết lập sự kiện click vào item
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId());  // Gửi ID của bài đăng
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imgPost;
        Button btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            imgPost = itemView.findViewById(R.id.img_post_image);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
