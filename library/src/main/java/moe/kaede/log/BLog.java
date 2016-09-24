/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * BLog allover Api
 *
 * @author kaede
 * @version date 16/9/22
 */

public class BLog {
    private static Logger mLogger;

    private BLog() {
    }

    private static boolean checkInit() {
        boolean init = mLogger != null;

        if (!init && LogSetting.DEBUG) {
            Log.w(LogSetting.TAG, "pls call Blog.initialize first!");
        }

        return init;
    }

    /**
     * You should call {@link BLog#initialize(Context)} before using BLog.
     */
    public static void initialize(Context context) {
        if (context == null) return;

        initialize(new LogSetting.Builder(context).build());
    }

    /**
     * You should call {@link BLog#initialize(LogSetting)} before using BLog.
     *
     * @param setting Custom config
     */
    public static void initialize(LogSetting setting) {
        if (setting == null) return;

        if (mLogger == null) {
            synchronized (BLog.class) {
                if (mLogger == null) {
                    mLogger = new Logger(setting);
                }
            }
        }
    }

    /**
     * You should call {@link BLog#shutdown()} before you call
     * {@link BLog#initialize(Context)} again.
     */
    public static void shutdown() {
        if (checkInit()) {
            mLogger.release();
            mLogger = null;
        }
    }

    public static void v(String tag, String message) {
        if (checkInit()) {
            mLogger.verbose(tag, message);
        }
    }

    public static void vfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.verbose(tag, fmt, args);
        }
    }

    public static void d(String tag, String message) {
        if (checkInit()) {
            mLogger.debug(tag, message);
        }
    }

    public static void d(String tag, Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.warn(tag, throwable, message);
        }
    }

    public static void dfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.debug(tag, fmt, args);
        }
    }

    public static void i(String tag, String message) {
        mLogger.info(tag, message);
    }

    public static void ifmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.info(tag, fmt, args);
        }
    }

    public static void w(String tag, String message) {
        if (checkInit()) {
            mLogger.warn(tag, message);
        }
    }

    public static void w(String tag, Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.warn(tag, throwable, message);
        }
    }

    public static void wfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.warn(tag, fmt, args);
        }
    }

    public static void e(String tag, String message) {
        if (checkInit()) {
            mLogger.error(tag, message);
        }
    }

    public static void e(String tag, Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.error(tag, throwable, message);
        }
    }

    public static void efmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.error(tag, fmt, args);
        }
    }

    public static void wtf(String tag, String message) {
        if (checkInit()) {
            mLogger.wtf(tag, message);
        }
    }

    public static void wtffmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.wtf(tag, fmt, args);
        }
    }

    public static void printStackTrace(String tag, Exception e) {
        if (checkInit()) {
            mLogger.warn(tag, e, null);
        }
    }

    public static void event(@NonNull String eventType, String message) {
        if (checkInit()) {
            mLogger.event(eventType, message);
        }
    }

    public static File[] getFilesByDate(Date date) {
        if (checkInit()) {
            return mLogger.queryFilesByDate(date.getTime());
        }
        return null;
    }

    public static File getLogDir() {
        if (checkInit()) {
            return new File(mLogger.getSetting().getLogDir());
        }
        return null;
    }

    public static void deleteLogs() {
        if (checkInit()) {
            InternalUtils.deleteQuietly(new File(mLogger.getSetting().getLogDir()));
        }
    }

    static Logger getLogger() {
        return mLogger;
    }

    static LogSetting getSetting() {
        if (checkInit()) {
            return mLogger.getSetting();
        }
        return null;
    }
}
