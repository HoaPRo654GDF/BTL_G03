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

public class PostLocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_location, container, false);



        // Khởi tạo và thiết lập bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
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

        String postId = getArguments().getString("postId");

        // Tải thông tin vị trí từ Firebase sử dụng postId
        if (postId != null) {
            loadPostLocationFromFirebase(postId);
        } else {
            Toast.makeText(getActivity(), "Không có postId", Toast.LENGTH_SHORT).show();
        }
        // Tải các vị trí bài đăng từ Firebase
    }
    private void loadPostLocationFromFirebase(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lấy dữ liệu latitude, longitude, title từ Firebase
                Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                Double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                String title = dataSnapshot.child("title").getValue(String.class);

                // Kiểm tra dữ liệu hợp lệ
                if (latitude != null && longitude != null && title != null) {
                    // Hiển thị vị trí lên bản đồ
                    LatLng postLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(postLocation).title(title));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 20));
                } else {
                    // Xử lý khi không có dữ liệu hợp lệ
                    Toast.makeText(getActivity(), "Không tìm thấy vị trí bài đăng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi truy vấn Firebase
                Toast.makeText(getActivity(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
