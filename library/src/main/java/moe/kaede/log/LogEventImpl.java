/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.annotation.WorkerThread;

import java.util.LinkedList;
import java.util.List;

class LogEventImpl implements Log {

    private static final int EVENT_TASK_ID = 0x333;

    private final String mFilePath;
    private final byte[] mLock = new byte[0];
    private final LogSetting mSetting;
    private final Files mFiles;
    private final List<Files.LogMessage> mCacheQueue;

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
        mFilePath = mFiles.getEventPath();
    }

    @Override
    public void log(int logType, String tag, String msg) {
        if (mSetting.getLogfileLevel() == LogLevel.NONE || mSetting.getLogfileLevel() > logType)
            return;

        // get logMessage from Object Pools
        Files.LogMessage logMessage = Files.LogMessage.obtain();
        logMessage.setMessage(logType, System.currentTimeMillis(), tag, msg);

        // add to list
        synchronized (mLock) {
            mCacheQueue.add(logMessage);
        }

        // write to file
        if (!Executor.instance().hasMessages(EVENT_TASK_ID)) {
            Executor.instance().postMessage(EVENT_TASK_ID, mWriteTask);
        }
    }


    @WorkerThread
    private void writeToFile() {
        if (mFiles.canWrite(mFilePath)) {
            List<Files.LogMessage> list;

            synchronized (mLock) {
                list = new LinkedList<>(mCacheQueue);
                mCacheQueue.clear();
            }

            mFiles.writeToFile(list, mFilePath);
        }
    }
}
