package com.example.btl_g03.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.btl_g03.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);



        // Khởi tạo và thiết lập bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Thiết lập vị trí mặc định (ví dụ Hà Nội)
        LatLng defaultLocation = new LatLng(21.028511, 105.804817); // Hà Nội
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));


        // Tải các vị trí bài đăng từ Firebase
         loadPostLocations();
    }

    private void loadPostLocations() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Duyệt qua tất cả các bài đăng trong Firebase
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Kiểm tra các giá trị latitude, longitude, title từ Firebase
                    Double latitude = postSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = postSnapshot.child("longitude").getValue(Double.class);
                    String title = postSnapshot.child("title").getValue(String.class);

                    // Kiểm tra nếu dữ liệu hợp lệ
                    if (latitude != null && longitude != null && title != null) {
                        // Tạo LatLng và thêm marker vào bản đồ
                        LatLng postLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(postLocation).title(title));

                        // Cập nhật vị trí camera nếu cần
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 12));
                    } else {
                        // Xử lý khi thiếu thông tin
                        // Có thể hiển thị thông báo lỗi hoặc làm gì đó ở đây
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi khi tải dữ liệu từ Firebase
                // Có thể hiển thị thông báo lỗi ở đây
            }
        });
    }
}
