package com.example.otpforwarder;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SmsReceiverTest {

    @Test
    public void testOtpRegexExtraction() {
        String msg = "Your OTP is 246810. Do not share it.";
        Pattern otpRegex = Pattern.compile("\\b\\d{4,8}\\b");
        Matcher matcher = otpRegex.matcher(msg);

        assertTrue(matcher.find());
        assertEquals("246810", matcher.group());
    }

    @Test
    public void testTrustedSenderFilter() {
        SmsReceiver receiver = new SmsReceiver();
        assertTrue(receiver.isTrustedSender("VK-MANTYS"));
        assertFalse(receiver.isTrustedSender("ICICI"));
        assertFalse(receiver.isTrustedSender(null));
    }
}
