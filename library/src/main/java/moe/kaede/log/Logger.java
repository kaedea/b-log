/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com)
 */

package moe.kaede.log;

import android.text.TextUtils;

import java.io.File;

class Logger {

    private final int mEventLevel;
    private final String mDefaultTag;
    private final LogSetting mSetting;
    private final Log mLogCatImpl;
    private final Log mLogFileImpl;
    private final Log mLogEventImpl;
    private final Files mFiles;

    public Logger(LogSetting setting) {
        mSetting = setting;
        mEventLevel = setting.getEventLevel();
        mDefaultTag = setting.getDefaultTag();

        if (setting.getLogcatLevel() != LogLevel.NONE) {
            mLogCatImpl = new LogCatImpl(setting);
        } else {
            mLogCatImpl = null;
        }

        if (setting.getLogfileLevel() != LogLevel.NONE) {
            mLogFileImpl = new LogFileImpl(setting);
            mLogEventImpl = new LogEventImpl(setting);
        } else {
            mLogFileImpl = null;
            mLogEventImpl = null;
        }

        mFiles = Files.instance(setting);
        cleanExpiredFiles();
    }

    public void release() {
        Files.release();
    }

    public void cleanExpiredFiles() {
        Executor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mFiles.cleanExpiredLogs();
                } catch (Throwable e) {
                    if (mSetting.debuggable()) {
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
        log(LogLevel.VERBOSE, ensureTag(tag), formatMessage(fmt, args));
    }

    public void verbose(String tag, Throwable throwable, String message) {
        log(LogLevel.VERBOSE, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * debug
     **/
    public void debug(String tag, String fmt, Object... args) {
        log(LogLevel.DEBUG, ensureTag(tag), formatMessage(fmt, args));
    }

    public void debug(String tag, Throwable throwable, String message) {
        log(LogLevel.DEBUG, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * info
     **/
    public void info(String tag, String fmt, Object... args) {
        log(LogLevel.INFO, ensureTag(tag), formatMessage(fmt, args));
    }

    public void info(String tag, Throwable throwable, String message) {
        log(LogLevel.INFO, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * warning
     **/
    public void warn(String tag, String fmt, Object... args) {
        log(LogLevel.WARN, ensureTag(tag), formatMessage(fmt, args));
    }

    public void warn(String tag, Throwable throwable, String message) {
        log(LogLevel.WARN, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * error
     **/
    public void error(String tag, String fmt, Object... args) {
        log(LogLevel.ERROR, ensureTag(tag), formatMessage(fmt, args));
    }

    public void error(String tag, Throwable throwable, String message) {
        log(LogLevel.ERROR, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * wtf
     **/
    public void wtf(String tag, String fmt, Object... args) {
        log(LogLevel.ASSERT, ensureTag(tag), formatMessage(fmt, args));
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
            if (mSetting.debuggable()) e.printStackTrace();

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
                + (throwable == null ? "null" : InternalUtils.getStackTraceString(throwable));
    }

    private void log(int logLevel, String tag, String message) {
        if (mLogCatImpl != null) {
            mLogCatImpl.log(logLevel, tag, message);
        }

        if (mLogFileImpl != null) {
            mLogFileImpl.log(logLevel, tag, message);
        }
    }

    private void event(int logLevel, String tag, String message) {
        if (mLogCatImpl != null) {
            mLogCatImpl.log(logLevel, tag, message);
        }

        if (mLogEventImpl != null) {
            mLogEventImpl.log(logLevel, tag, message);
        }
    }

    public LogSetting getSetting() {
        return mSetting;
    }

    public File[] queryFilesByDate(int mode, long ms) {
        if (mFiles != null) {
            return mFiles.queryFilesByDate(mode, ms);
        }
        return null;
    }

    public File[] queryFiles(int mode) {
        if (mFiles != null) {
            return mFiles.queryFiles(mode);
        }
        return null;
    }

    public File zippingFiles(int mode) {
        File zipFile = new File(mFiles.getZipPath(mode));
        InternalUtils.deleteQuietly(zipFile);

        if (InternalUtils.zippingFiles(queryFiles(mode), zipFile)) {
            return zipFile;
        }
        return null;
    }

    public File zippingFiles(int mode, long ms) {
        File zipFile = new File(mFiles.getZipPath(mode));
        InternalUtils.deleteQuietly(zipFile);

        if (InternalUtils.zippingFiles(queryFilesByDate(mode, ms), zipFile)) {
            return zipFile;
        }
        return null;
    }
}
