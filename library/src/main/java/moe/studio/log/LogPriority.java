/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.util.Log;

/**
 * Log Priority, see {@link LogSetting}
 *
 * @author kaede
 * @version date 16/9/22
 */

@SuppressWarnings("WeakerAccess")
public class LogPriority {

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;
    public static final int NONE = ASSERT + 1; // Do not log.

    /**
     * Get name for the current log level.
     */
    public static String getName(int priority) {
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

    /**
     * Whether the given log level in valid.
     */
    public static boolean isValid(int priority) {
        return (priority >= VERBOSE && priority <= NONE);
    }
}
