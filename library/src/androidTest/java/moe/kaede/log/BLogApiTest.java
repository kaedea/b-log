/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author kaede
 * @version date 16/9/25
 */
@RunWith(AndroidJUnit4.class)
public class BLogApiTest {

    public static final String TAG = "BLogApiTest";

    @Before
    public void setUp() {
        BLog.initialize(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void shutDown() {
        BLog.shutdown();
    }

    @Test
    public void useLogBasic() {
        BLog.v(TAG, "log verbose");
        BLog.v("log verbose with default tag");

        BLog.d(TAG, "log debug");
        BLog.d("log debug with default tag");

        BLog.i(TAG, "log info");
        BLog.i("log info with default tag");

        BLog.w(TAG, "log warning");
        BLog.w("log warning with default tag");

        BLog.e(TAG, "log error");
        BLog.e("log error with default tag");

        BLog.wtf(TAG, "log wtf");
        BLog.wtf("log wtf with default tag");
    }

    @Test
    public void useLogFormat() {
        // use log format, you must offer a tag, even it's null(use default tag)
        BLog.vfmt(TAG, "log %s with format string", "verbose");
        BLog.dfmt(null, "log %s with format string", "debug");
        BLog.ifmt(TAG, "log %s with format string", "info");
        BLog.wfmt(null, "log %s with format string", "warning");
        BLog.efmt(TAG, "log %s with format string", "error");
        BLog.wtffmt(null, "log %s with format string", "wtf");

        // test error format args
        // 1. error format msg
        BLog.dfmt(null, "error format msg", "debug");
        // 2. error format args
        BLog.dfmt(null, "%s format msg", "error", "error", "error");
        BLog.dfmt(null, "%s %s %s format msg", "error");
    }

    @Test
    public void useLogThrowable() {
        Exception exception = new RuntimeException("一种钦定的感觉");

        BLog.v(TAG, exception, "runtime exception");
        BLog.v(exception, null);

        BLog.d(TAG, exception, "runtime exception");
        BLog.d(exception, null);

        BLog.i(TAG, exception, "runtime exception");
        BLog.i(exception, null);

        BLog.w(TAG, exception, "runtime exception");
        BLog.w(exception, null);

        BLog.e(TAG, exception, "runtime exception");
        BLog.e(exception, null);
    }
}
