/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com)
 */

package moe.kaede.log;

import android.text.TextUtils;

class LogCatImpl implements Log {

    private static final int CHUNK_SIZE = 4000;

    private final String mEmptyMessage;
    private final LogSetting mSetting;
    private final boolean mShowThreadInfo;

    public LogCatImpl(LogSetting setting) {
        mSetting = setting;
        mShowThreadInfo = setting.isShowThreadInfo();
        mEmptyMessage = setting.getLogFormatter().emptyMessage();
    }

    @Override
    public void log(int priority, String tag, String msg) {
        if (mSetting.getLogcatLevel() == LogLevel.NONE || mSetting.getLogcatLevel() > priority) {
            return;
        }

        // AndroidLogcat may abort long message, just in case.
        separateMessageIfNeed(priority, tag, msg);
    }

    private void separateMessageIfNeed(int priority, String tag, String msg) {
        if (TextUtils.isEmpty(msg)) {
            logMessage(priority, tag, mEmptyMessage);
            return;
        }

        byte[] bytes = msg.getBytes();
        int length = bytes.length;

        if (length <= CHUNK_SIZE) {
            logMessage(priority, tag, msg);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            logMessage(priority, tag, new String(bytes, i, count));
        }
    }

    private void logMessage(int priority, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            if (mShowThreadInfo) {
                line = "[" + Thread.currentThread().getName() + "]  " + line;
            }
            logcat(priority, tag, line);
        }
    }

    private void logcat(int priority, String tag, String chunk) {
        android.util.Log.println(priority, tag, chunk);
    }
}
