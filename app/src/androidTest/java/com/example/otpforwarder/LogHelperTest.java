package com.example.otpforwarder;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;

public class LogHelperTest {

    @Test
    public void testLogHelperDoesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        LogHelper.log(context, "TEST", "VK-MANTYS", "1234", "Dummy message");
    }
}
