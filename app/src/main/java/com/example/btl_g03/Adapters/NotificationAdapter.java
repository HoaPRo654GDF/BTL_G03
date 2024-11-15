package com.example.btl_g03.Adapters;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_g03.Models.Notification;
import com.example.btl_g03.R;
import com.example.btl_g03.Activities.PostDetailActivity; // Hoạt động hiển thị bài đăng
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private Context context;

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.notificationContent.setText(notification.getContent());
        String requestId = notification.getRequestId();
        String userId = notification.getUserId();
        if (requestId != null && requestId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // Thông báo cho người bán
            holder.notificationContent.setText("Thông báo cho người bán: " + notification.getContent());
        } else if (userId != null && userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // Thông báo cho người nhận
            holder.notificationContent.setText("Thông báo cho người nhận: " + notification.getContent());
        }

        // Thêm sự kiện click
        holder.itemView.setOnClickListener(v -> {
            // Chuyển đến PostDetailActivity với postId từ thông báo
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", notification.getPostId()); // Gửi postId
            intent.putExtra("fromNotification", "true");
            intent.putExtra("requestId", requestId);  // ID yêu cầu

            // Sử dụng PendingIntent để mở PostDetailActivity khi người dùng nhấn vào thông báo
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                // Thực hiện PendingIntent, không gọi startActivity ở đây nữa
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationContent = itemView.findViewById(R.id.notificationContent);
        }
    }
}
