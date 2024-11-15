package com.example.btl_g03.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_g03.Adapters.NotificationAdapter;
import com.example.btl_g03.Models.Notification;
import com.example.btl_g03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = rootView.findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, getContext());
        recyclerView.setAdapter(adapter);

        loadNotifications();

        return rootView;
    }

    private void loadNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // UID của người dùng hiện tại
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Lọc thông báo cho người nhận (userId == currentUserId)
        notificationsRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        notificationList.clear();  // Xóa danh sách trước khi thêm mới thông báo
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);  // Thêm thông báo vào danh sách
                            }
                        }
                        adapter.notifyDataSetChanged();  // Cập nhật RecyclerView
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("NotificationFragment", "loadNotifications:onCancelled", databaseError.toException());
                    }
                });

        // Lọc thông báo cho người bán (requestId == currentUserId)
        notificationsRef.orderByChild("receiverId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null && !notificationList.contains(notification)) {
                                notificationList.add(notification);  // Thêm thông báo vào danh sách
                            }
                        }
                        adapter.notifyDataSetChanged();  // Cập nhật RecyclerView
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("NotificationFragment", "loadNotifications:onCancelled", databaseError.toException());
                    }
                });
    }
}
