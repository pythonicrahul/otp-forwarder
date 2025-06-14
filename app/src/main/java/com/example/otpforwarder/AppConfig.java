package com.example.otpforwarder;

import java.util.regex.Pattern;

public class AppConfig {

    // ✅ OTP regex
    public static final Pattern OTP_REGEX = Pattern.compile("\\b\\d{4,8}\\b");

    // ✅ Trusted TPAs
    public static final String[] TRUSTED_SENDERS = {
            "MANTYS", "MNTYS"
    };

    // ✅ Drop OTPs older than 3 min
    public static final long OTP_EXPIRY_MILLIS = 3 * 60 * 1000;

    // ✅ Default webhook
    public static final String WEBHOOK_URL = "https://otp-forwarder-server.onrender.com/otp";

    // ✅ CSV filename
    public static final String CSV_FILENAME = "otp_logs.csv";
}
