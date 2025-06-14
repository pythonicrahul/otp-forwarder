package com.example.otpforwarder;
import static com.example.otpforwarder.AppConfig.TRUSTED_SENDERS;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.regex.Matcher;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                for (Object pdu : pdus) {
                    String format = bundle.getString("format");
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                    String sender = sms.getDisplayOriginatingAddress();
                    String message = sms.getMessageBody();

                    Log.d(TAG, "SMS received from: " + sender);
                    Log.d(TAG, "Message: " + message);

                    if (isTrustedSender(sender)) {
                        Matcher matcher = AppConfig.OTP_REGEX.matcher(message);
                        if (matcher.find()) {
                            String otpCode = matcher.group();
                            Log.d(TAG, "Extracted OTP: " + otpCode);
                            LogHelper.log(context, "OTP_RECEIVED", sender, otpCode, "Message captured");
                            Utils.showToast(context, "OTP from " + sender + ": " + otpCode);
                            OtpSender.enqueueOtp(context, sender, otpCode);
                        } else {
                            Log.d(TAG, "No OTP found in message.");
                        }
                    } else {
                        Utils.showToast(context, "Did not matched message: " + message);
                        Log.d(TAG, "Ignored sender: " + sender);
                    }
                }
            }
        }
    }

    public static boolean isTrustedSender(String sender) {
        if (sender == null) return false;
        for (String trusted : TRUSTED_SENDERS) {
            if (sender.contains(trusted)) return true;
        }
        return false;
    }
}
