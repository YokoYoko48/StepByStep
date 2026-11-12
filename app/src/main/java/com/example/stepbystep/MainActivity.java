package com.example.stepbystep;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // ตัวแปร UI หน้าแรก
    private TextView tvStepCount, tvShakeCount;
    private TextView tvDuration, tvStepsPerMin, tvShakesPerMin;
    private TextView tvTotalSteps, tvTotalShakes; // เพิ่มตัวแปรแสดงยอดสะสม
    private Button btnSaveActivity;

    // ตัวแปร RecyclerView และ Database
    private RecyclerView recyclerViewHistory;
    private ActivityAdapter activityAdapter;
    private DBHelper dbHelper;
    private List<ActivityItem> activityList;

    // ตัวแปร Sensor
    private SensorManager sensorManager;
    private Sensor stepSensor, accelSensor;

    // ตัวแปรนับจำนวน
    private int stepCount = 0;
    private int shakeCount = 0;
    private long lastShakeTime = 0;

    // ตัวแปรจับเวลา (Timer)
    private long startTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    // รหัสขอ Permission
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ผูกตัวแปรกับ UI
        tvStepCount = findViewById(R.id.tvStepCount);
        tvShakeCount = findViewById(R.id.tvShakeCount);
        tvDuration = findViewById(R.id.tvDuration);
        tvStepsPerMin = findViewById(R.id.tvStepsPerMin);
        tvShakesPerMin = findViewById(R.id.tvShakesPerMin);
        tvTotalSteps = findViewById(R.id.tvTotalSteps);       // ผูกยอดสะสมก้าว
        tvTotalShakes = findViewById(R.id.tvTotalShakes);   // ผูกยอดสะสมเขย่า
        btnSaveActivity = findViewById(R.id.btnSaveActivity);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);

        // 2. ตั้งค่า Database และ RecyclerView
        dbHelper = new DBHelper(this);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        loadHistoryList(); // โหลดข้อมูลประวัติและยอดสะสมขึ้นมาแสดง

        // 3. ขอ Permission การนับก้าว
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_RECOGNITION_REQUEST_CODE);
        }

        // 4. เตรียม SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // 5. ระบบจับเวลา Real-time
        startTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                float totalMinutes = millis / 60000.0f;
                if (totalMinutes > 0) {
                    float stepsPerMin = stepCount / totalMinutes;
                    float shakesPerMin = shakeCount / totalMinutes;
                    tvStepsPerMin.setText(String.format(Locale.getDefault(), "%.1f", stepsPerMin));
                    tvShakesPerMin.setText(String.format(Locale.getDefault(), "%.1f", shakesPerMin));
                }

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);

        // 6. คลิกปุ่ม "บันทึกกิจกรรม" เพื่อเซฟลง SQLite
        btnSaveActivity.setOnClickListener(v -> {
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            String durationStr = tvDuration.getText().toString();

            // เรียกใช้ DBHelper เพื่อบันทึกข้อมูล
            boolean isInserted = dbHelper.insertActivity(currentDate, stepCount, shakeCount, durationStr);

            if (isInserted) {
                Toast.makeText(MainActivity.this, "บันทึกกิจกรรมสำเร็จ!", Toast.LENGTH_SHORT).show();
                loadHistoryList(); // โหลดข้อมูลรีเฟรช RecyclerView และยอดสะสมใหม่ทันที
            } else {
                Toast.makeText(MainActivity.this, "บันทึกไม่สำเร็จ ลองใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // เมธอดสำหรับดึงข้อมูลจาก Database มาใส่ RecyclerView และอัปเดตยอดสะสมรวม
    private void loadHistoryList() {
        activityList = dbHelper.getAllActivities();
        activityAdapter = new ActivityAdapter(this, activityList);
        recyclerViewHistory.setAdapter(activityAdapter);

        // ดึงยอดสะสมรวมจาก SQLite มาแสดงผล
        int totalSteps = dbHelper.getTotalSteps();
        int totalShakes = dbHelper.getTotalShakes();
        tvTotalSteps.setText(totalSteps + " ก้าว");
        tvTotalShakes.setText(totalShakes + " ครั้ง");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (stepSensor != null) sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            if (accelSensor != null) sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++;
            tvStepCount.setText(String.valueOf(stepCount));
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // แก้ไขจุดที่เครื่องหมายคูณขาดหายไป
            float gForce = (float) Math.sqrt((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH));

            if (gForce > 2.5) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > 500) {
                    lastShakeTime = currentTime;
                    shakeCount++;
                    tvShakeCount.setText(String.valueOf(shakeCount));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "อนุญาตการนับก้าวแล้ว", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "กรุณาอนุญาตระบบนับก้าวเพื่อใช้งานแอป", Toast.LENGTH_SHORT).show();
            }
        }
    }
}