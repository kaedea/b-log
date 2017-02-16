/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * BLog Apis test, acting as usage demo.
 *
 * @author kaede
 * @version date 16/9/25
 */
@RunWith(AndroidJUnit4.class)
public class BLogApiTest {

    public static final String TAG = "BLogApiTest";

    @Before
    public void setUp() {
        LogSetting setting = new LogSetting.Builder(InstrumentationRegistry.getTargetContext())
                .showThreadInfo(true)
                .debuggable(true)
                .build();
        BLog.initialize(setting);
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

        BLog.v(TAG, "runtime exception", exception);
        BLog.v(null, exception);

        BLog.d(TAG, "runtime exception", exception);
        BLog.d(null, exception);

        BLog.i(TAG, "runtime exception", exception);
        BLog.i(null, exception);

        BLog.w(TAG, "runtime exception", exception);
        BLog.w(null, exception);

        BLog.e(TAG, "runtime exception", exception);
        BLog.e(null, exception);
    }

    @Test
    public void useEvent() {
        BLog.event("BLOG", "XX一律不得经商");
        BLog.event("XX发大财才是坠猴的");
        BLog.event("做了一点成绩");
    }
}
