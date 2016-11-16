/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com)
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
    static final String DEFAULT_DIR = "blog";

    // QUERY MODE
    public static final int LOG = 0x0001;
    public static final int EVENT = 0x0010;

    private int mExpiredDay;
    private int mLogcatLevel;
    private int mLogfileLevel;
    private int mEventLevel;
    private boolean mShowThreadInfo;
    private boolean mDebuggable;
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

    public boolean debuggable() {
        return mDebuggable;
    }

    public static class Builder {
        private Context mContext;
        private int mExpiredDay;
        private int mLogcatLevel = -1;
        private int mLogfileLevel = -1;
        private int mEventLevel;
        private boolean mShowThreadInfo;
        private String mLogDir;
        private String mDefaultTag;
        private LogFormatter mFormatter;
        private boolean mDebuggable;

        public Builder(Context context) {
            mContext = context;
            mExpiredDay = 2;
            mEventLevel = LogLevel.INFO;
            mDefaultTag = "BLOG";
            mDebuggable = BuildConfig.DEBUG;
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

        public Builder setLogcatLevel(int priority) {
            if (LogLevel.isLevelValid(priority)) {
                mLogcatLevel = priority;
            }
            return this;
        }

        public Builder setLogfileLevel(int priority) {
            if (LogLevel.isLevelValid(priority)) {
                mLogfileLevel = priority;
            }
            return this;
        }

        public Builder setEventLevel(int priority) {
            mEventLevel = priority;
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

        public Builder debuggable(boolean debuggable) {
            mDebuggable = debuggable;
            return this;
        }

        public LogSetting build() {
            LogSetting setting = new LogSetting();
            setting.mLogDir = mLogDir;
            setting.mDebuggable = mDebuggable;
            setting.mExpiredDay = mExpiredDay;
            setting.mLogcatLevel = mLogcatLevel;
            setting.mLogfileLevel = mLogfileLevel;
            setting.mEventLevel = mEventLevel;
            setting.mDefaultTag = mDefaultTag;
            setting.mFormatter = mFormatter;
            setting.mShowThreadInfo = mShowThreadInfo;

            if (setting.mLogcatLevel == -1) {
                setting.mLogcatLevel = mDebuggable ? LogLevel.VERBOSE : LogLevel.ERROR;
            }
            if (setting.mLogfileLevel == -1) {
                setting.mLogfileLevel = mDebuggable ? LogLevel.DEBUG : LogLevel.INFO;
            }
            if (setting.mFormatter == null) {
                setting.mFormatter = new LogFormatterImpl(setting);
            }

            if (TextUtils.isEmpty(setting.mLogDir)) {
                File rootDir = null;
                boolean external = Environment.MEDIA_MOUNTED
                        .equals(Environment.getExternalStorageState());

                if (external) {
                    try {
                        rootDir = mContext.getExternalFilesDir(DEFAULT_DIR);
                    } catch (Throwable e) {
                        if (mDebuggable) {
                            e.printStackTrace();
                        }
                    }
                }

                if (rootDir == null) {
                    if (mDebuggable) {
                        Log.w(TAG, "create external log dir fail, do you miss the permission?");
                    }
                    rootDir = mContext.getDir(DEFAULT_DIR, Context.MODE_PRIVATE);
                }

                InternalUtils.createDir(rootDir);
                setting.mLogDir = rootDir.getAbsolutePath();
            }

            return setting;
        }
    }
}
