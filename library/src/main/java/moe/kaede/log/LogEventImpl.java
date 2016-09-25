/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

import static moe.kaede.log.LogLevel.NONE;

class LogEventImpl implements Log {

    private static final int EVENT_TASK_ID = 0x333;

    private final String EVENT_FILE_PATH;
    private final List<String> mCacheQueue;
    private final byte[] mLock = new byte[0];
    private final LogSetting mSetting;
    private final Files mFiles;
    private final LogFormatter mFormatter;

    private final Runnable mWriteTask = new Runnable() {
        @Override
        public void run() {
            writeToFile();
        }
    };

    public LogEventImpl(LogSetting setting) {
        mSetting = setting;
        mFiles = Files.instance(setting);
        mCacheQueue = new LinkedList<>();
        EVENT_FILE_PATH = mFiles.getEventPath();
        mFormatter = setting.getLogFormatter();
    }

    @Override
    public void log(int logType, String tag, String msg) {
        if (mSetting.getLogfileLevel() == NONE || mSetting.getLogfileLevel() > logType) return;

        String message = buildEventString(logType, System.currentTimeMillis(), tag, msg);

        // add to list
        synchronized (mLock) {
            mCacheQueue.add(message);
        }

        // write to file
        if (!Executor.instance().hasMessages(EVENT_TASK_ID)) {
            Executor.instance().postMessage(EVENT_TASK_ID, mWriteTask);
        }
    }

    private String buildEventString(int logType, long time, String tag, String msg) {
        if (TextUtils.isEmpty(msg))
            msg = mFormatter.emptyMessage();

        return mFormatter.buildMessage(logType, time, tag, msg);
    }

    // @WorkerThread
    private void writeToFile() {
        if (mFiles.canWrite(EVENT_FILE_PATH)) {
            mFiles.writeToFile(mCacheQueue, EVENT_FILE_PATH);
        }
    }
}
