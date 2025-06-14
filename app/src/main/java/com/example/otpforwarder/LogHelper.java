package com.example.otpforwarder;

import android.content.Context;
import android.os.AsyncTask;

public class LogHelper {

    public static void log(Context context, String eventType, String sender, String otp, String message) {
        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            EventLogDao dao = db.logDao();

            EventLog log = new EventLog();
            log.timestamp = System.currentTimeMillis();
            log.eventType = eventType;
            log.sender = sender;
            log.otp = otp;
            log.message = message;

            dao.insert(log);
            dao.deleteOldLogs(); // Keep only latest 100
        });
    }
}
