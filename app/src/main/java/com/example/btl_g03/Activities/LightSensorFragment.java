package com.example.btl_g03.Activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.btl_g03.R;

public class LightSensorFragment extends Fragment implements SensorEventListener {
    private TextView lightValueText;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private float initialValue = 0.0f;
    private boolean hasInitialReading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light_sensor, container, false);

        lightValueText = view.findViewById(R.id.lightValue);

        // Khởi tạo sensor
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Kiểm tra cảm biến
        if (lightSensor == null) {
            lightValueText.setText("Thiết bị không có cảm biến ánh sáng");
        }
        Button btnRefresh = view.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            hasInitialReading = false; // Reset flag
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lightSensor != null && !hasInitialReading) {
            // Chỉ đăng ký listener nếu chưa có giá trị ban đầu
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!hasInitialReading) {
            initialValue = event.values[0];
            hasInitialReading = true;
            lightValueText.setText(String.format("Độ sáng: %.1f lux", initialValue));
            // Hủy đăng ký listener sau khi đã có giá trị
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }
}