package com.example.otpforwarder;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;
import android.app.AlertDialog;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestSmsPermissions();
        findViewById(R.id.exportButton).setOnClickListener(v -> exportCsvToDownloads());
        SwitchCompat toggleSwitch = findViewById(R.id.toggleService);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean("otp_enabled", true);
        toggleSwitch.setChecked(isEnabled);

        setReceiverEnabled(isEnabled);

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("otp_enabled", isChecked).apply();
            setReceiverEnabled(isChecked);
        });

        findViewById(R.id.testButton).setOnClickListener(v -> {
            String testSender = "TEST-MANTYS";
            String testOtp = "123456";

            LogHelper.log(this, "TEST_OTP", testSender, testOtp, "Test OTP triggered by user");
            OtpSender.enqueueOtp(this, testSender, testOtp);

            Utils.showToast(this, "Test OTP sent");
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSystemState();
    }


    private void setReceiverEnabled(boolean enabled) {
        ComponentName receiver = new ComponentName(this, SmsReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(
                receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }


    private void exportCsvToDownloads() {
        AsyncTask.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<EventLog> logs = db.logDao().getLatestLogs();

                String filename = AppConfig.CSV_FILENAME + System.currentTimeMillis() + ".csv";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to create file URI", Toast.LENGTH_LONG).show());
                    return;
                }

                try (OutputStream out = resolver.openOutputStream(uri);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {

                    writer.write("Timestamp,Event Type,Sender,OTP,Message\n");

                    for (EventLog log : logs) {
                        writer.write(log.timestamp + "," +
                                safe(log.eventType) + "," +
                                safe(log.sender) + "," +
                                safe(log.otp) + "," +
                                safe(log.message) + "\n");
                    }

                    writer.flush();

                    runOnUiThread(() ->
                            Toast.makeText(this, "CSV exported to Downloads", Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        });
    }

    private String safe(String input) {
        return input == null ? "" : input.replace(",", " "); // avoid breaking CSV
    }

    private void checkAndRequestSmsPermissions() {
        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        boolean permissionsNeeded = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded = true;
                break;
            }
        }

        if (permissionsNeeded) {
            ActivityCompat.requestPermissions(this, permissions, 101);
        }
    }


    private void checkSystemState() {
        // 1. Check SMS Permission Again (in case revoked after install)
        boolean hasReceiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (!hasReceiveSms) {
            Toast.makeText(this, "SMS permission required. Please enable it from Settings.", Toast.LENGTH_LONG).show();
        }

        // 2. Check Battery Optimization
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isIgnoring = pm.isIgnoringBatteryOptimizations(getPackageName());

        if (!isIgnoring) {
            new AlertDialog.Builder(this)
                    .setTitle("Disable Battery Optimization")
                    .setMessage("To ensure OTPs are received in the background, please disable battery optimization for this app.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
