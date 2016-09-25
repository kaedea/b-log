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
 * BLog is an Android LogCat extended Utility. It can simplify the way you use
 * {@link android.util.Log}, as well as write our log message into file for after support.
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

    /** verbose **/
    public static void v(String message) {
        if (checkInit()) {
            mLogger.verbose(null, message);
        }
    }

    public static void v(String tag, String message) {
        if (checkInit()) {
            mLogger.verbose(tag, message);
        }
    }

    public static void v(Throwable throwable,  String message) {
        if (checkInit()) {
            mLogger.verbose(null, throwable, message);
        }
    }

    public static void v(String tag, Throwable throwable,  String message) {
        if (checkInit()) {
            mLogger.verbose(tag, throwable, message);
        }
    }

    public static void vfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.verbose(tag, fmt, args);
        }
    }

    /** debug **/
    public static void d(String message) {
        if (checkInit()) {
            mLogger.debug(null, message);
        }
    }

    public static void d(String tag, String message) {
        if (checkInit()) {
            mLogger.debug(tag, message);
        }
    }

    public static void d(Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.debug(null, throwable, message);
        }
    }

    public static void d(String tag, Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.debug(tag, throwable, message);
        }
    }

    public static void dfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.debug(tag, fmt, args);
        }
    }

    /** info **/
    public static void i(String message) {
        if (checkInit()) {
            mLogger.info(null, message);
        }
    }

    public static void i(String tag, String message) {
        if (checkInit()) {
            mLogger.info(tag, message);
        }
    }

    public static void i(Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.info(null, throwable, message);
        }
    }

    public static void i(String tag, Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.info(tag, throwable, message);
        }
    }

    public static void ifmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            mLogger.info(tag, fmt, args);
        }
    }

    /** warning **/
    public static void w(String message) {
        if (checkInit()) {
            mLogger.warn(null, message);
        }
    }

    public static void w(String tag, String message) {
        if (checkInit()) {
            mLogger.warn(tag, message);
        }
    }

    public static void w(Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.warn(null, throwable, message);
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

    /** warning **/
    public static void e(String message) {
        if (checkInit()) {
            mLogger.error(null, message);
        }
    }

    public static void e(String tag, String message) {
        if (checkInit()) {
            mLogger.error(tag, message);
        }
    }

    public static void e(Throwable throwable, String message) {
        if (checkInit()) {
            mLogger.error(null, throwable, message);
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

    /** wtf **/
    public static void wtf(String message) {
        if (checkInit()) {
            mLogger.wtf(null, message);
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
