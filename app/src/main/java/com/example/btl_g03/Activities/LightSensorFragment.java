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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.btl_g03.R;

public class LightSensorFragment extends Fragment implements SensorEventListener {
    private TextView lightValueText;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light_sensor, container, false);
        
        lightValueText = view.findViewById(R.id.lightValue);
        
        // Khởi tạo sensor
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Kiểm tra xem thiết bị có cảm biến ánh sáng không
        if (lightSensor == null) {
            Toast.makeText(getContext(), "Thiết bị không có cảm biến ánh sáng!", Toast.LENGTH_SHORT).show();
            lightValueText.setText("Không có cảm biến ánh sáng");
        } else {
            // Hiển thị giá trị mặc định
            lightValueText.setText("Giá trị mặc định: 0.0 lux");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lightSensor != null) {
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
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightValue = event.values[0];
            lightValueText.setText(String.format("Độ sáng hiện tại: %.1f lux", lightValue));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }
}