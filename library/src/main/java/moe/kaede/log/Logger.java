/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import static moe.kaede.log.LogLevel.ASSERT;
import static moe.kaede.log.LogLevel.DEBUG;
import static moe.kaede.log.LogLevel.ERROR;
import static moe.kaede.log.LogLevel.INFO;
import static moe.kaede.log.LogLevel.VERBOSE;
import static moe.kaede.log.LogLevel.WARN;

class Logger {

    private final int mEventLevel;
    private final String mDefaultTag;
    private final LogSetting mLogSetting;
    private final LogCatImpl mLogCatImpl;
    private final LogFileImpl mLogFileImpl;
    private final LogEventImpl mLogEventImpl;
    private final Files mFiles;

    public Logger(LogSetting setting) {
        mLogSetting = setting;
        mEventLevel = setting.getEventLevel();
        mDefaultTag = setting.getDefaultTag();
        mLogCatImpl = new LogCatImpl(setting);
        mLogFileImpl = new LogFileImpl(setting);
        mLogEventImpl = new LogEventImpl(setting);
        mFiles = Files.instance(setting);

        cleanExpiredFiles();
    }

    public void release() {
        Files.release();
    }

    public void cleanExpiredFiles() {
        Executor.instance().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mFiles.cleanExpiredLogs();
                } catch (Exception e) {
                    if (LogSetting.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * verbose
     **/
    public void verbose(String tag, String fmt, Object... args) {
        log(VERBOSE, ensureTag(tag), formatMessage(fmt, args));
    }

    public void verbose(String tag, Throwable throwable, String message) {
        log(VERBOSE, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * debug
     **/
    public void debug(String tag, String fmt, Object... args) {
        log(DEBUG, ensureTag(tag), formatMessage(fmt, args));
    }

    public void debug(String tag, Throwable throwable, String message) {
        log(DEBUG, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * info
     **/
    public void info(String tag, String fmt, Object... args) {
        log(INFO, ensureTag(tag), formatMessage(fmt, args));
    }

    public void info(String tag, Throwable throwable, String message) {
        log(INFO, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * warning
     **/
    public void warn(String tag, String fmt, Object... args) {
        log(WARN, ensureTag(tag), formatMessage(fmt, args));
    }

    public void warn(String tag, Throwable throwable, String message) {
        log(WARN, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * error
     **/
    public void error(String tag, String fmt, Object... args) {
        log(ERROR, ensureTag(tag), formatMessage(fmt, args));
    }

    public void error(String tag, Throwable throwable, String message) {
        log(ERROR, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * wtf
     **/
    public void wtf(String tag, String fmt, Object... args) {
        log(ASSERT, ensureTag(tag), formatMessage(fmt, args));
    }

    /**
     * event
     **/
    public void event(String tag, String message) {
        event(mEventLevel, ensureTag(tag), message);
    }

    private String ensureTag(String tag) {
        return TextUtils.isEmpty(tag) ? mDefaultTag : tag;
    }

    private String formatMessage(String fmt, Object... args) {
        if (args == null) return fmt;

        String message;
        try {
            message = String.format(fmt, args);
            return message;

        } catch (Throwable e) {
            if (LogSetting.DEBUG) e.printStackTrace();

            StringBuilder sb = new StringBuilder("format error, fmt = " + String.valueOf(fmt)
                    + ", args = ");
            for (int i = 0; i < args.length; i++) {
                Object item = args[i];
                sb.append(String.valueOf(item));
                if (i != (args.length - 1)) sb.append(", ");
            }

            return sb.toString();
        }
    }

    private String formatThrowable(String message, Throwable throwable) {
        return String.valueOf(message) + " : "
                + (throwable == null ? "null" : Log.getStackTraceString(throwable));
    }

    private void log(int logLevel, String tag, String message) {
        mLogCatImpl.log(logLevel, tag, message);

        if (mLogFileImpl != null) {
            mLogFileImpl.log(logLevel, tag, message);
        }
    }

    private void event(int logLevel, String tag, String message) {
        mLogCatImpl.log(logLevel, tag, message);

        if (mLogEventImpl != null) {
            mLogEventImpl.log(logLevel, tag, message);
        }
    }

    public LogSetting getSetting() {
        return mLogSetting;
    }

    public File[] queryFilesByDate(long ms) {
        if (mLogFileImpl != null) {
            return mFiles.queryFilesByDate(ms);
        }
        return null;
    }
}
