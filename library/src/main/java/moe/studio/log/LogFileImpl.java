/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import moe.studio.log.Files.LogMessage;

@SuppressWarnings("WeakerAccess")
class LogFileImpl implements Log {

    private static final int LOG_TASK_ID = 0x222;

    private int mWriteCount;
    private final byte[] mLock = new byte[0];
    private final LogSetting mSetting;
    private final Files mFiles;
    private final File mLogFile;
    private final List<LogMessage> mCacheQueue;

    private final Runnable mWriteTask = new Runnable() {
        @Override
        public void run() {
            writeToFile();
        }
    };

    public LogFileImpl(LogSetting setting) {
        mSetting = setting;
        mFiles = Files.instance(setting);
        mCacheQueue = new LinkedList<>();
        mLogFile = mFiles.getLogFile();

        try {
            InternalUtils.checkCreateFile(mLogFile);
        } catch (IOException e) {
            Logger.w("Can not create file.", e);
        }
    }

    @Override
    public void log(int priority, String tag, String msg) {
        if (mSetting.getLogfilePriority() == LogPriority.NONE
                || mSetting.getLogfilePriority() > priority) {
            return;
        }

        // get logMessage from Object Pools
        LogMessage logMessage = LogMessage.obtain();
        logMessage.setMessage(priority, System.currentTimeMillis(), tag, Thread.currentThread().getName(), msg);

        // add to list
        synchronized (mLock) {
            mCacheQueue.add(logMessage);
        }

        // write to file
        if (!Executor.instance().hasMessages(LOG_TASK_ID)) {
            Executor.instance().postMessage(LOG_TASK_ID, mWriteTask);
        }
    }

    @Override
    public void onShutdown() {
        if (mSetting.debuggable()) {
            Logger.w("LogFile is shutdown, file written count = " + mWriteCount);
        }
    }

    @WorkerThread
    private void writeToFile() {
        if (mFiles.canWrite(mLogFile)) {
            List<LogMessage> list;

            synchronized (mLock) {
                list = new LinkedList<>(mCacheQueue);
                mCacheQueue.clear();
            }

            mFiles.writeToFile(list, mLogFile);
            if (mSetting.debuggable()) {
                mWriteCount ++;
            }
        }
    }
}
