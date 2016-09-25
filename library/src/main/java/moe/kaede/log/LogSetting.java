/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * BLog config class, use {@link LogSetting.Builder} to custom your config.
 *
 * @author kaede
 * @version date 16/9/22
 */

public class LogSetting {

    static final String TAG = "blog";
    static final boolean DEBUG = BuildConfig.DEBUG;
    static final String DEFAULT_DIR = "blog";

    private int mExpiredDay;
    private int mLogcatLevel;
    private int mLogfileLevel;
    private int mEventLevel;
    private boolean mShowThreadInfo;
    private String mLogDir;
    private String mDefaultTag;
    private LogFormatter mFormatter;

    private LogSetting() {
    }

    public int getLogcatLevel() {
        return mLogcatLevel;
    }

    public int getLogfileLevel() {
        return mLogfileLevel;
    }

    public int getEventLevel() {
        return mEventLevel;
    }

    public String getLogDir() {
        return mLogDir;
    }

    public int getExpiredDay() {
        return mExpiredDay;
    }

    public LogFormatter getLogFormatter() {
        return mFormatter;
    }

    public String getDefaultTag() {
        return mDefaultTag;
    }

    public boolean isShowThreadInfo() {
        return mShowThreadInfo;
    }

    public static class Builder {
        private int mExpiredDay;
        private int mLogcatLevel;
        private int mLogfileLevel;
        private int mEventLevel;
        private boolean mShowThreadInfo;
        private String mLogDir;
        private String mDefaultTag;
        private LogFormatter mFormatter;

        public Builder(Context context) {
            mExpiredDay = 2;
            mLogcatLevel = BuildConfig.DEBUG ? LogLevel.VERBOSE : LogLevel.ERROR;
            mLogfileLevel = BuildConfig.DEBUG ? LogLevel.DEBUG : LogLevel.ERROR;
            mEventLevel = LogLevel.INFO;
            mDefaultTag = "BLOG";

            File rootDir = null;
            boolean external = Environment.MEDIA_MOUNTED
                    .equals(Environment.getExternalStorageState());

            if (external) {
                try {
                    rootDir = context.getExternalFilesDir(DEFAULT_DIR);
                } catch (Throwable e) {
                    if (DEBUG) {
                        Log.w(TAG, "create external log dir fail, do you miss the permission? : "
                                + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (rootDir == null) {
                rootDir = context.getDir(DEFAULT_DIR, Context.MODE_PRIVATE);
            }

            InternalUtils.createDir(rootDir);
            mLogDir = rootDir.getAbsolutePath();
        }

        public Builder setLogDir(String path) {
            if (!TextUtils.isEmpty(path)) {
                mLogDir = path;
            }
            return this;
        }

        public Builder setDefaultTag(String defaultTag) {
            this.mDefaultTag = defaultTag;
            return this;
        }

        public Builder setLogcatLevel(int level) {
            if (LogLevel.isLevelValid(level)) {
                mLogcatLevel = level;
            }
            return this;
        }

        public Builder setLogfileLevel(int level) {
            if (LogLevel.isLevelValid(level)) {
                mLogfileLevel = level;
            }
            return this;
        }

        public Builder setEventLevel(int eventLevel) {
            mEventLevel = eventLevel;
            return this;
        }

        public Builder setExpiredDay(int expiredDay) {
            if (mExpiredDay > 0) {
                mExpiredDay = expiredDay;
            }
            return this;
        }

        public Builder setFormatter(LogFormatter formatter) {
            mFormatter = formatter;
            return this;
        }

        public Builder showThreadInfo(boolean showThreadInfo) {
            mShowThreadInfo = showThreadInfo;
            return this;
        }

        public LogSetting build() {
            LogSetting setting = new LogSetting();
            setting.mExpiredDay = mExpiredDay;
            setting.mLogcatLevel = mLogcatLevel;
            setting.mLogfileLevel = mLogfileLevel;
            setting.mEventLevel = mEventLevel;
            setting.mLogDir = mLogDir;
            setting.mDefaultTag = mDefaultTag;
            setting.mShowThreadInfo = mShowThreadInfo;
            setting.mFormatter = mFormatter;

            if (this.mFormatter == null) {
                setting.mFormatter = new LogFormatterImpl(setting);
            }

            return setting;
        }
    }
}
