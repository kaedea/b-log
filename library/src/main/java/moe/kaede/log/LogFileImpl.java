/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

import static moe.kaede.log.LogLevel.NONE;

class LogFileImpl implements Log {

    private static final int LOG_TASK_ID = 0x222;

    private final String LOG_FILE_PATH;
    private final List<String> mLogCache;
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

    public LogFileImpl(LogSetting setting) {
        mSetting = setting;
        mFiles = Files.instance(setting);
        mLogCache = new LinkedList<>();
        LOG_FILE_PATH = mFiles.getLogPath();
        mFormatter = setting.getLogFormatter();
    }

    @Override
    public void log(int logType, String tag, String msg) {
        if (mSetting.getLogfileLevel() == NONE || mSetting.getLogfileLevel() > logType) return;

        String message = buildLogString(logType, System.currentTimeMillis(), tag, msg);

        // add to list
        synchronized (mLock) {
            mLogCache.add(message);
        }

        // write to file
        if (!Executor.instance().hasMessages(LOG_TASK_ID)) {
            Executor.instance().postMessage(LOG_TASK_ID, mWriteTask);
        }
    }

    private String buildLogString(int logType, long time, String tag, String msg) {
        if (TextUtils.isEmpty(msg))
            msg = mFormatter.emptyMessage();

        return mFormatter.buildMessage(logType, time, tag, msg);
    }

    // @WorkerThread
    private void writeToFile() {
        if (mFiles.canWrite(LOG_FILE_PATH)) {
            mFiles.writeToFile(mLogCache, LOG_FILE_PATH);
        }
    }
}
