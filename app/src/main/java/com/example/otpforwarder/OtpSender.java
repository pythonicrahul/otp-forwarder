package com.example.otpforwarder;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class OtpSender {

    public static void enqueueOtp(Context context, String tpa, String code) {
        long timestamp = System.currentTimeMillis();

        Data inputData = new Data.Builder()
                .putString("tpa", tpa)
                .putString("code", code)
                .putLong("ts", timestamp)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SendOtpWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
