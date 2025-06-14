package com.example.otpforwarder;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

public class OtpSenderTest {

    @Test
    public void testEnqueueOtpRunsWithoutCrash() {
        Context context = ApplicationProvider.getApplicationContext();

        // Just ensures that no exception is thrown
        OtpSender.enqueueOtp(context, "VK-MANTYS", "123456");
    }
}
