package com.example.otpforwarder;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;

public class SendOtpWorker extends Worker {

    private static final String TAG = "SendOtpWorker";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();


    public SendOtpWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String tpa = getInputData().getString("tpa");
            String code = getInputData().getString("code");
            long ts = getInputData().getLong("ts", 0);

            // Drop if older than 3 minutes
            long now = System.currentTimeMillis();
            if (now - ts > AppConfig.OTP_EXPIRY_MILLIS) {
                Log.w(TAG, "OTP too old. Dropping...");
                return Result.success(); // drop it silently
            }

            JSONObject json = new JSONObject();
            json.put("tpa", tpa);
            json.put("code", code);
            json.put("ts", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(new Date(ts)));

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder().url(AppConfig.WEBHOOK_URL).post(body).build();

            Log.d(TAG, "Sending OTP via WorkManager: " + json.toString());

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "OTP sent successfully");
                LogHelper.log(getApplicationContext(), "SEND_SUCCESS", tpa, code, "Sent to webhook");
                long receivedAt = getInputData().getLong("ts", 0L);
                long now_ = System.currentTimeMillis();

                if (receivedAt > 0) {
                    long roundTrip = now_ - receivedAt;
                    Utils.showToast(getApplicationContext(), "OTP sent in " + roundTrip + " ms");
                }

                return Result.success();
            } else {
                Log.e(TAG, "Failed with code: " + response.code());
                LogHelper.log(getApplicationContext(), "SEND_FAIL", tpa, code, "Failed to send: " + response.code());
                return Result.retry(); // automatic retry
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception sending OTP", e);
            return Result.retry(); // retry on failure
        }
    }
}
