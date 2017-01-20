/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
class LogEngine {

    private final int mEventPriority;
    private final String mDefaultTag;
    private final LogSetting mSetting;
    private final Log mLogCatImpl;
    private final Log mLogFileImpl;
    private final Log mLogFileSyncImpl;
    private final Log mLogEventImpl;
    private final Log mLogAdapter;
    private final Files mFiles;

    public LogEngine(LogSetting setting) {
        mSetting = setting;
        mEventPriority = setting.getEventPriority();
        mDefaultTag = setting.getDefaultTag();
        mLogAdapter = setting.getAdapter();

        if (setting.getLogcatPriority() != LogPriority.NONE) {
            mLogCatImpl = new LogCatImpl(setting);
        } else {
            mLogCatImpl = null;
        }

        if (setting.getLogfilePriority() != LogPriority.NONE) {
            mLogFileImpl = new LogFileImpl(setting);
            mLogEventImpl = new LogEventImpl(setting);
            mLogFileSyncImpl = new LogFileSyncImpl(setting);
        } else {
            mLogFileImpl = null;
            mLogEventImpl = null;
            mLogFileSyncImpl = null;
        }

        mFiles = Files.instance(setting);
        cleanExpiredFiles();
    }

    public void shutdown() {
        if (mLogCatImpl != null) {
            mLogCatImpl.onShutdown();
        }
        if (mLogFileImpl != null) {
            mLogFileImpl.onShutdown();
        }
        if (mLogEventImpl != null) {
            mLogEventImpl.onShutdown();
        }
        if (mLogFileSyncImpl != null) {
            mLogFileSyncImpl.onShutdown();
        }
        if (mLogAdapter != null) {
            mLogAdapter.onShutdown();
        }

        Files.release();
    }

    public void cleanExpiredFiles() {
        Executor.instance().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mFiles.cleanExpiredLogs();
                } catch (Throwable e) {
                    Logger.w(e);
                }
            }
        });
    }

    /**
     * verbose
     **/
    public void verbose(String tag, String fmt, Object... args) {
        log(LogPriority.VERBOSE, ensureTag(tag), formatMessage(fmt, args));
    }

    public void verbose(String tag, Throwable throwable, String message) {
        log(LogPriority.VERBOSE, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * debug
     **/
    public void debug(String tag, String fmt, Object... args) {
        log(LogPriority.DEBUG, ensureTag(tag), formatMessage(fmt, args));
    }

    public void debug(String tag, Throwable throwable, String message) {
        log(LogPriority.DEBUG, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * info
     **/
    public void info(String tag, String fmt, Object... args) {
        log(LogPriority.INFO, ensureTag(tag), formatMessage(fmt, args));
    }

    public void info(String tag, Throwable throwable, String message) {
        log(LogPriority.INFO, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * warning
     **/
    public void warn(String tag, String fmt, Object... args) {
        log(LogPriority.WARN, ensureTag(tag), formatMessage(fmt, args));
    }

    public void warn(String tag, Throwable throwable, String message) {
        log(LogPriority.WARN, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * error
     **/
    public void error(String tag, String fmt, Object... args) {
        log(LogPriority.ERROR, ensureTag(tag), formatMessage(fmt, args));
    }

    public void error(String tag, Throwable throwable, String message) {
        log(LogPriority.ERROR, ensureTag(tag), formatThrowable(message, throwable));
    }

    /**
     * wtf
     **/
    public void wtf(String tag, String fmt, Object... args) {
        log(LogPriority.ASSERT, ensureTag(tag), formatMessage(fmt, args));
    }

    /**
     * event
     **/
    public void event(String tag, String message) {
        event(mEventPriority, ensureTag(tag), message);
    }

    public void syncLog(int priority, String tag, String message) {
        sync(priority, ensureTag(tag), message);
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
            Logger.w(e);

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

    private void log(int priority, String tag, String message) {
        if (mLogCatImpl != null) {
            mLogCatImpl.log(priority, tag, message);
        }

        if (mLogAdapter != null) {
            mLogAdapter.log(priority, tag, message);
        }

        if (mLogFileImpl != null) {
            mLogFileImpl.log(priority, tag, message);
        }
    }

    private void event(int priority, String tag, String message) {
        if (mLogCatImpl != null) {
            mLogCatImpl.log(priority, tag, message);
        }

        if (mLogAdapter != null) {
            mLogAdapter.log(priority, tag, message);
        }

        if (mLogEventImpl != null) {
            mLogEventImpl.log(priority, tag, message);
        }
    }

    private void sync(int priority, String tag, String message) {
        if (mLogCatImpl != null) {
            mLogCatImpl.log(priority, tag, message);
        }

        if (mLogAdapter != null) {
            mLogAdapter.log(priority, tag, message);
        }

        if (mLogFileSyncImpl != null) {
            mLogFileSyncImpl.log(priority, tag, message);
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

    public File zippingFiles(int mode, List<File> attaches) {
        File zipFile = mFiles.getZipFile(mode);
        if (zipFile == null) {
            Logger.w("Log directory is null.");
            return null;
        }

        InternalUtils.delete(zipFile);
        List<File> files = Arrays.asList(queryFiles(mode));

        if (attaches != null) {
            List<File> newList = new ArrayList<>(files);
            newList.addAll(attaches);
            files = newList;
        }

        if (InternalUtils.zippingFiles(files, zipFile)) {
            return zipFile;
        }

        return null;
    }

    public File zippingFiles(int mode, long ms, List<File> attaches) {
        File zipFile = mFiles.getZipFile(mode);
        if (zipFile == null) {
            Logger.w("Log directory is null.");
            return null;
        }

        InternalUtils.delete(zipFile);
        List<File> files = Arrays.asList(queryFilesByDate(mode, ms));

        if (attaches != null) {
            List<File> newList = new ArrayList<>(files);
            newList.addAll(attaches);
            files = newList;
        }

        if (InternalUtils.zippingFiles(files, zipFile)) {
            return zipFile;
        }

        return null;
    }
}
