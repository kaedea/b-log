/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.content.Context;
import android.support.annotation.IntRange;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * BLog config class, use {@link LogSetting.Builder} to custom your config.
 *
 * @author kaede
 * @version date 16/9/22
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class LogSetting {

    static final String DEFAULT_DIR = "blog";

    // QUERY MODE
    public static final int LOG = 0x0001;
    public static final int EVENT = 0x0010;

    private int mExpiredDay;
    private int mLogcatPriority;
    private int mLogfilePriority;
    private int mEventPriority;
    private boolean mShowThreadInfo;
    private boolean mDebuggable;
    private String mDefaultTag;
    private File mLogDir;
    private LogFormatter mFormatter;
    private Log mAdapter;

    private LogSetting() {
    }

    /**
     * Get level to check whether to logcat or not.
     */
    public int getLogcatPriority() {
        return mLogcatPriority;
    }

    /**
     * Get level to check whether to log file or not.
     */
    public int getLogfilePriority() {
        return mLogfilePriority;
    }

    /**
     * Get level to check whether to log event or not.
     */
    public int getEventPriority() {
        return mEventPriority;
    }

    /**
     * Get log files' base dir, using {@link #getLogcatPriority()} instead.
     */
    @Deprecated
    public String getLogDir() {
        return mLogDir.getAbsolutePath();
    }

    /**
     * Get log files' base dir.
     */
    public File getLogDirectory() {
        return mLogDir;
    }

    /**
     * Get log files' expired day.
     */
    public int getExpiredDay() {
        return mExpiredDay;
    }

    /**
     * Get log format.
     */
    public LogFormatter getLogFormatter() {
        return mFormatter;
    }

    /**
     * Get default tag.
     */
    public String getDefaultTag() {
        return mDefaultTag;
    }

    /**
     * Whether or not to show thread info.
     */
    public boolean isShowThreadInfo() {
        return mShowThreadInfo;
    }

    /**
     * Whether it is debug mode.
     */
    public boolean debuggable() {
        return mDebuggable;
    }

    /**
     * Get user {@link Log} implement.
     */
    public Log getAdapter() {
        return mAdapter;
    }

    public static class Builder {

        private Context mContext;
        private int mExpiredDay;
        private int mLogcatPriority = -1;
        private int mLogfilePriority = -1;
        private int mEventPriority;
        private boolean mShowThreadInfo;
        private String mDefaultTag;
        private File mLogDir;
        private LogFormatter mFormatter;
        private Log mAdapter;
        private boolean mDebuggable;

        public Builder(Context context) {
            mContext = context;
            mExpiredDay = 2;
            mEventPriority = LogPriority.INFO;
            mDefaultTag = "BLOG";
            mDebuggable = BuildConfig.DEBUG;
        }

        /**
         * Set log files' base dir, using {@link #setLogDirectory(File)} instead.
         */
        @Deprecated
        public Builder setLogDir(String path) {
            if (!TextUtils.isEmpty(path)) {
                mLogDir = new File(path);
            }
            return this;
        }
        /**
         * Set log files' base dir.
         */
        public Builder setLogDirectory(File dir) {
            mLogDir = dir;
            return this;
        }

        /**
         * Set default tag.
         */
        public Builder setDefaultTag(String defaultTag) {
            this.mDefaultTag = defaultTag;
            return this;
        }

        /**
         * Set level to check whether to logcat or not.
         */
        public Builder setLogcatPriority(@IntRange(from = LogPriority.VERBOSE, to = LogPriority.NONE)
                                                 int priority) {
            if (LogPriority.isValid(priority)) {
                mLogcatPriority = priority;
            } else {
                throw new RuntimeException("Priority is invalid.");
            }
            return this;
        }

        /**
         * Set level to check whether to log file or not.
         */
        public Builder setLogfilePriority(@IntRange(from = LogPriority.VERBOSE, to = LogPriority.NONE)
                                                  int priority) {
            if (LogPriority.isValid(priority)) {
                mLogfilePriority = priority;
            } else {
                throw new RuntimeException("Priority is invalid.");
            }
            return this;
        }

        /**
         * Set level to check whether to log file or not.
         */
        public Builder setEventPriority(int priority) {
            if (LogPriority.isValid(priority)) {
                mEventPriority = priority;
            } else {
                throw new RuntimeException("Priority is invalid.");
            }
            return this;
        }

        /**
         * Set days to keep the current log file.
         */
        public Builder setExpiredDay(int expiredDay) {
            if (mExpiredDay > 0) {
                mExpiredDay = expiredDay;
            } else {
                throw new RuntimeException("Expired day is invalid.");
            }
            return this;
        }

        /**
         * Set log format.
         */
        public Builder setFormatter(LogFormatter formatter) {
            mFormatter = formatter;
            return this;
        }

        /**
         * Set whether to show thread info in log or not.
         */
        public Builder showThreadInfo(boolean showThreadInfo) {
            mShowThreadInfo = showThreadInfo;
            return this;
        }

        /**
         * Set whether it is debug mode.
         */
        public Builder debuggable(boolean debuggable) {
            mDebuggable = debuggable;
            return this;
        }

        /**
         * Set user {@link Log} implement.
         */
        public Builder setAdapter(Log adapter) {
            mAdapter = adapter;
            return this;
        }

        public LogSetting build() {

            LogSetting setting = new LogSetting();
            setting.mLogDir = mLogDir;
            setting.mDebuggable = mDebuggable;
            setting.mExpiredDay = mExpiredDay;
            setting.mLogcatPriority = mLogcatPriority;
            setting.mLogfilePriority = mLogfilePriority;
            setting.mEventPriority = mEventPriority;
            setting.mDefaultTag = mDefaultTag;
            setting.mFormatter = mFormatter;
            setting.mShowThreadInfo = mShowThreadInfo;
            setting.mAdapter = mAdapter;

            if (setting.mLogcatPriority == -1) {
                setting.mLogcatPriority = mDebuggable ? LogPriority.VERBOSE : LogPriority.ERROR;
            }
            if (setting.mLogfilePriority == -1) {
                setting.mLogfilePriority = mDebuggable ? LogPriority.DEBUG : LogPriority.INFO;
            }
            if (setting.mFormatter == null) {
                setting.mFormatter = new LogFormatterImpl(setting);
            }

            if (mLogDir == null) {
                File logDir = null;

                try {
                    logDir = mContext.getExternalFilesDir(DEFAULT_DIR);
                } catch (Throwable e) {
                    if (mDebuggable) {
                        Logger.w(e);
                    }
                }

                if (logDir == null) {
                    if (mDebuggable) {
                        Logger.w("Create external log dir fail, do you miss the permission?");
                    }
                    logDir = mContext.getDir(DEFAULT_DIR, Context.MODE_PRIVATE);
                }

                setting.mLogDir = logDir;
            }

            try {
                InternalUtils.checkCreateDir(setting.mLogDir);
            } catch (IOException e) {
                setting.mLogDir = null;
                Logger.w(e);
            }

            return setting;
        }
    }
}
