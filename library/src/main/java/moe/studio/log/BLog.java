/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.content.Context;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * BLog is an Android LogCat extended Utility. It can simplify the way you use
 * {@link android.util.Log}, as well as write our log message into file for after support.
 *
 * @author Kaede
 * @version date 16/9/22
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class BLog {

    private static LogEngine sLogEngine;

    private BLog() {
    }

    private static boolean checkInit() {
        boolean init = sLogEngine != null;

        if (!init) {
            throw new RuntimeException("Pls call Blog.initialize first!");
        }

        return true;
    }

    /**
     * You should call this method before using BLog.
     */
    public static void initialize(Context context) {
        if (context == null) {
            throw new RuntimeException("Context is null.");
        }

        initialize(new LogSetting.Builder(context).build());
    }

    /**
     * You should call this method before using BLog.
     *
     * @param setting Custom config
     */
    public static void initialize(LogSetting setting) {
        if (setting == null) {
            throw new RuntimeException("Setting is null.");
        }

        if (sLogEngine == null) {
            synchronized (BLog.class) {
                if (sLogEngine == null) {
                    sLogEngine = new LogEngine(setting);
                }
            }
        }
    }

    /**
     * You should call this method before you call {@link BLog#initialize(Context)} again.
     */
    public static void shutdown() {
        if (checkInit()) {
            sLogEngine.shutdown();
            sLogEngine = null;
        }
    }

    /**
     * Verbose log.
     */
    public static void v(String message) {
        if (checkInit()) {
            sLogEngine.verbose(null, message);
        }
    }

    public static void v(String tag, String message) {
        if (checkInit()) {
            sLogEngine.verbose(tag, message);
        }
    }

    public static void v(String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.verbose(null, throwable, message);
        }
    }

    public static void v(String tag, String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.verbose(tag, throwable, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void vfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.verbose(tag, fmt, args);
        }
    }

    /**
     * Debug log.
     */
    public static void d(String message) {
        if (checkInit()) {
            sLogEngine.debug(null, message);
        }
    }

    public static void d(String tag, String message) {
        if (checkInit()) {
            sLogEngine.debug(tag, message);
        }
    }

    public static void d(String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.debug(null, throwable, message);
        }
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.debug(tag, throwable, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void dfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.debug(tag, fmt, args);
        }
    }

    /**
     * info
     **/
    public static void i(String message) {
        if (checkInit()) {
            sLogEngine.info(null, message);
        }
    }

    public static void i(String tag, String message) {
        if (checkInit()) {
            sLogEngine.info(tag, message);
        }
    }

    public static void i(String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.verbose(null, throwable, message);
        }
    }

    public static void i(String tag, String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.info(tag, throwable, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void ifmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.info(tag, fmt, args);
        }
    }

    /**
     * warning
     **/
    public static void w(String message) {
        if (checkInit()) {
            sLogEngine.warn(null, message);
        }
    }

    public static void w(String tag, String message) {
        if (checkInit()) {
            sLogEngine.warn(tag, message);
        }
    }

    public static void w(String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.warn(null, throwable, message);
        }
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.warn(tag, throwable, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void wfmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.warn(tag, fmt, args);
        }
    }

    /**
     * warning
     **/
    public static void e(String message) {
        if (checkInit()) {
            sLogEngine.error(null, message);
        }
    }

    public static void e(String tag, String message) {
        if (checkInit()) {
            sLogEngine.error(tag, message);
        }
    }

    public static void e(String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.error(null, throwable, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (checkInit()) {
            sLogEngine.error(tag, throwable, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void efmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.error(tag, fmt, args);
        }
    }

    /**
     * wtf
     **/
    public static void wtf(String message) {
        if (checkInit()) {
            sLogEngine.wtf(null, message);
        }
    }

    public static void wtf(String tag, String message) {
        if (checkInit()) {
            sLogEngine.wtf(tag, message);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void wtffmt(String tag, String fmt, Object... args) {
        if (checkInit()) {
            sLogEngine.wtf(tag, fmt, args);
        }
    }

    /**
     * Log event, logging message in an unique file.
     * Note that this api will log message in logcat according to {@link LogSetting#getEventPriority()}.
     **/
    public static void event(String message) {
        if (checkInit()) {
            sLogEngine.event(null, message);
        }
    }

    /**
     * See {@linkplain #event(String)}.
     **/
    public static void event(String tag, String message) {
        if (checkInit()) {
            sLogEngine.event(tag, message);
        }
    }

    /**
     * Sync log message to file.
     */
    @WorkerThread
    public static void syncLog(int priority, String message) {
        if (checkInit()) {
            sLogEngine.syncLog(priority, null, message);
        }
    }

    /**
     * See {@linkplain #syncLog(int, String)}.
     **/
    @WorkerThread
    public static void syncLog(int priority, String tag, String message) {
        if (checkInit()) {
            sLogEngine.syncLog(priority, tag, message);
        }
    }

    /**
     * others
     **/

    /**
     * get all log files
     *
     * @param mode mode for filtering log files, support '|' operation,
     *             see {@link LogSetting#LOG}, {@link LogSetting#EVENT}
     */
    public static File[] getLogFiles(int mode) {
        if (checkInit()) {
            return sLogEngine.queryFiles(mode);
        }
        return null;
    }

    /**
     * get log files by day
     *
     * @param date retain null if today
     */
    public static File[] getLogFilesByDate(int mode, Date date) {
        if (checkInit()) {
            if (date == null) {
                date = new Date(); // today
            }
            return sLogEngine.queryFilesByDate(mode, date.getTime());
        }
        return null;
    }

    @Deprecated
    @WorkerThread
    public static File zippingLogFiles(int mode) {
        return zippingLogFiles(mode, null);
    }

    @Deprecated
    @WorkerThread
    public static File zippingLogFilesByDate(int mode, Date date) {
        return zippingLogFilesByDate(mode, date, null);
    }

    /**
     * Zipping log files and return the zip file.
     */
    @WorkerThread
    public static File zippingLogFiles(int mode, List<File> attaches) {
        if (checkInit()) {
            return sLogEngine.zippingFiles(mode, attaches);
        }
        return null;
    }

    /**
     * See {@linkplain #zippingLogFiles(int)}.
     **/
    @WorkerThread
    public static File zippingLogFilesByDate(int mode, Date date, List<File> attaches) {
        if (checkInit()) {
            if (date == null) {
                date = new Date(); // today
            }
            return sLogEngine.zippingFiles(mode, date.getTime(), attaches);
        }
        return null;
    }

    /**
     * Get log file's directory.
     */
    public static File getLogDir() {
        if (checkInit()) {
            return sLogEngine.getSetting().getLogDirectory();
        }
        return null;
    }

    /**
     * Delete existing log files.
     */
    public static void deleteLogs() {
        if (checkInit()) {
            InternalUtils.delete(sLogEngine.getSetting().getLogDirectory());
        }
    }

    /**
     * Package accessible for testcase.
     */
    static LogEngine getLogger() {
        return sLogEngine;
    }

    /**
     * Package accessible for testcase.
     */
    static LogSetting getSetting() {
        if (checkInit()) {
            return sLogEngine.getSetting();
        }
        return null;
    }
}
