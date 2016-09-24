/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.text.TextUtils;

import static moe.kaede.log.LogLevel.ASSERT;
import static moe.kaede.log.LogLevel.DEBUG;
import static moe.kaede.log.LogLevel.ERROR;
import static moe.kaede.log.LogLevel.INFO;
import static moe.kaede.log.LogLevel.NONE;
import static moe.kaede.log.LogLevel.VERBOSE;
import static moe.kaede.log.LogLevel.WARN;

class LogCatImpl implements Log {

    private static final int CHUNK_SIZE = 4000;
    private static final String EMPTY_MSG = "Empty/NULL";
    private final LogSetting mSetting;

    public LogCatImpl(LogSetting setting) {
        mSetting = setting;
    }

    @Override
    public void log(int logLevel, String tag, String msg) {
        if (mSetting.getLogcatLevel() == NONE || mSetting.getLogcatLevel() > logLevel)
            return;

        // AndroidLogcat may abort long message, just in case.
        separateMessageIfNeed(logLevel, tag, msg);
    }

    private void separateMessageIfNeed(int logType, String tag, String msg) {
        if (TextUtils.isEmpty(msg)) {
            logMessage(logType, tag, EMPTY_MSG);
            return;
        }

        byte[] bytes = msg.getBytes();
        int length = bytes.length;

        if (length <= CHUNK_SIZE) {
            logMessage(logType, tag, msg);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            logMessage(logType, tag, new String(bytes, i, count));
        }
    }

    private void logMessage(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logcat(logType, tag, line);
        }
    }

    private void logcat(int logType, String tag, String chunk) {
        switch (logType) {
            case VERBOSE:
                android.util.Log.v(tag, chunk);
                break;
            case DEBUG:
                android.util.Log.d(tag, chunk);
                break;
            case INFO:
                android.util.Log.i(tag, chunk);
                break;
            case WARN:
                android.util.Log.w(tag, chunk);
                break;
            case ERROR:
                android.util.Log.e(tag, chunk);
                break;
            case ASSERT:
                android.util.Log.wtf(tag, chunk);
                break;
            default:
                android.util.Log.d(tag, chunk);
                break;
        }
    }
}
