/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
class LogEventImpl implements Log {

    private static final int EVENT_TASK_ID = 0x333;

    private int mWriteCount;
    private final byte[] mLock = new byte[0];
    private final LogSetting mSetting;
    private final Files mFiles;
    private final File mEventFile;
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
        mEventFile = mFiles.getEventFile();

        try {
            InternalUtils.checkCreateFile(mEventFile);
        } catch (IOException e) {
            Logger.w("Can not create file.", e);
        }
    }

    @Override
    public void log(int priority, String tag, String msg) {
        if (mSetting.getLogfilePriority() == LogPriority.NONE || mSetting.getLogfilePriority() > priority)
            return;

        // get logMessage from Object Pools
        Files.LogMessage logMessage = Files.LogMessage.obtain();
        logMessage.setMessage(priority, System.currentTimeMillis(), tag, Thread.currentThread().getName(), msg);

        // add to list
        synchronized (mLock) {
            mCacheQueue.add(logMessage);
        }

        // write to file
        if (!Executor.instance().hasMessages(EVENT_TASK_ID)) {
            Executor.instance().postMessage(EVENT_TASK_ID, mWriteTask);
        }
    }

    @Override
    public void onShutdown() {
        if (mSetting.debuggable()) {
            Logger.w("LogEvent is shutdown, file written count = " + mWriteCount);
        }
    }


    @WorkerThread
    private void writeToFile() {
        if (mFiles.canWrite(mEventFile)) {
            List<Files.LogMessage> list;

            synchronized (mLock) {
                list = new LinkedList<>(mCacheQueue);
                mCacheQueue.clear();
            }

            mFiles.writeToFile(list, mEventFile);
            if (mSetting.debuggable()) {
                mWriteCount ++;
            }
        }
    }
}
