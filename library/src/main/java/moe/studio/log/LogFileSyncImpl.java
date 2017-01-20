/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
class LogFileSyncImpl implements Log {

    private final LogSetting mSetting;
    private final File mLogFile;
    private final Files mFiles;

    public LogFileSyncImpl(LogSetting setting) {
        mSetting = setting;
        mFiles = Files.instance(setting);
        mLogFile = mFiles.getLogFile();

        try {
            InternalUtils.checkCreateFile(mLogFile);
        } catch (IOException e) {
            Logger.w("Can not create file.", e);
        }
    }

    @WorkerThread
    public void log(int priority, String tag, String msg) {
        if (mSetting.getLogfilePriority() == LogPriority.NONE
                || mSetting.getLogfilePriority() > priority) {
            return;
        }

        // get logMessage from Object Pools
        Files.LogMessage logMessage = Files.LogMessage.obtain();
        logMessage.setMessage(priority, System.currentTimeMillis(), tag,
                Thread.currentThread().getName(), msg);

        if (mFiles.canWrite(mLogFile)) {
            mFiles.writeToFile(logMessage, mLogFile);
        }
    }

    @Override
    public void onShutdown() {

    }
}
