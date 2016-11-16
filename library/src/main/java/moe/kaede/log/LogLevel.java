/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com)
 */

package moe.kaede.log;

import android.util.Log;

/**
 * Log Level, see {@link LogSetting}
 *
 * @author kaede
 * @version date 16/9/22
 */

public class LogLevel {
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;
    public static final int NONE = ASSERT + 1;

    public static String getLevelName(int priority) {
        switch (priority) {
            case VERBOSE:
                return "VERBOSE";
            case DEBUG:
                return "DEBUG";
            case INFO:
                return "INFO";
            case WARN:
                return "WARN";
            case ERROR:
                return "ERROR";
            case ASSERT:
                return "ASSERT";
            case NONE:
                return "NONE";
            default:
                return "UNKNOWN";
        }
    }

    public static boolean isLevelValid(int level) {
        return (level >= VERBOSE && level <= NONE);
    }
}
