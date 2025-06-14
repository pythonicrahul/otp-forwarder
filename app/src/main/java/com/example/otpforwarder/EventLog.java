package com.example.otpforwarder;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EventLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public String eventType;
    public String sender;
    public String otp;
    public String message;
}
