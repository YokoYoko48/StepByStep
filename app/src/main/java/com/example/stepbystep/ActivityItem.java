package com.example.stepbystep;

public class ActivityItem {
    private int id;
    private String date;
    private int steps;
    private int shakes;
    private String duration;

    // Constructor สำหรับตอนดึงข้อมูลจาก Database (มี ID แล้ว)
    public ActivityItem(int id, String date, int steps, int shakes, String duration) {
        this.id = id;
        this.date = date;
        this.steps = steps;
        this.shakes = shakes;
        this.duration = duration;
    }

    // Constructor สำหรับตอนสร้างข้อมูลใหม่เพื่อบันทึก (ยังไม่มี ID ระบบจะรันอัตโนมัติ)
    public ActivityItem(String date, int steps, int shakes, String duration) {
        this.date = date;
        this.steps = steps;
        this.shakes = shakes;
        this.duration = duration;
    }

    // Getters เอาไว้ดึงค่าไปแสดงผลบน UI
    public int getId() { return id; }
    public String getDate() { return date; }
    public int getSteps() { return steps; }
    public int getShakes() { return shakes; }
    public String getDuration() { return duration; }
}