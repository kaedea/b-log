/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
class Files {

    private static Files sInstance;

    static final String LOG_FILE_EXTENSION = ".log";
    static final String EVENT_FILE_EXTENSION = ".event";
    static final String ZIP_FILE_EXTENSION = ".zip";
    static final String FILE_HYPHEN = "-";

    private final LogSetting mSetting;
    private final LogFormatter mFormatter;
    private final SimpleDateFormat mNameFormatter = new SimpleDateFormat(
            "yyyyMMdd", Locale.getDefault());
    private final byte[] mLock = new byte[0];

    private Files(LogSetting setting) {
        mSetting = setting;
        mFormatter = setting.getLogFormatter();
    }

    public static Files instance(LogSetting setting) {
        if (sInstance == null) {
            synchronized (Files.class) {
                if (sInstance == null) {
                    sInstance = new Files(setting);
                }
            }
        }
        return sInstance;
    }

    public static void release() {
        sInstance = null;
    }

    // ROOT_DIR/20160927-main.log
    @Nullable
    public File getLogFile() {
        if (mSetting.getLogDirectory() == null) {
            return null;
        }
        String date = mNameFormatter.format(System.currentTimeMillis());
        return new File(mSetting.getLogDirectory(), date + FILE_HYPHEN + InternalUtils.getProcessName()
                + LOG_FILE_EXTENSION);
    }

    // ROOT_DIR/20160927-main.event
    @Nullable
    public File getEventFile() {
        if (mSetting.getLogDirectory() == null) {
            return null;
        }
        String date = mNameFormatter.format(System.currentTimeMillis());
        return new File(mSetting.getLogDirectory(), date + FILE_HYPHEN + InternalUtils.getProcessName()
                + EVENT_FILE_EXTENSION) ;
    }

    // ROOT_DIR/20160927-all.zip
    @Nullable
    public File getZipFile(int mode) {
        if (mSetting.getLogDirectory() == null) {
            return null;
        }

        String suffix;
        switch (mode) {
            case LogSetting.EVENT:
                suffix = "event";
                break;
            case LogSetting.LOG:
                suffix = "log";
                break;
            case LogSetting.LOG | LogSetting.EVENT:
            default:
                suffix = "all";
                break;
        }

        String date = mNameFormatter.format(System.currentTimeMillis());
        return new File(mSetting.getLogDirectory(), date + FILE_HYPHEN + suffix
                + ZIP_FILE_EXTENSION);
    }

    public boolean canWrite(File file) {
        try {
            InternalUtils.checkCreateFile(file);
        } catch (IOException e) {
            Logger.w( "Can not create file.", e);
            return false;
        }

        if (!file.canWrite()) {
            Logger.w("Log file is not allowed to be written.");
            return false;
        }

        return true;
    }

    @WorkerThread
    public void writeToFile(List<LogMessage> logMessages, File file) {
        if (!file.exists()) {
            Logger.w("Log file not exist, can not write!");
            return;
        }

        RandomAccessFile lockRaf = null;
        FileChannel lockChannel = null;
        FileLock fileLock = null;
        PrintWriter printWriter = null;

        try {
            lockRaf = new RandomAccessFile(file, "rw");
            lockChannel = lockRaf.getChannel();
            fileLock = lockChannel.lock();

            synchronized (mLock) {
                FileOutputStream fos = new FileOutputStream(file, true);
                OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
                printWriter = new PrintWriter(writer);

                for (LogMessage logMessage : logMessages) {
                    printWriter.println(logMessage.buildMessage(mFormatter));
                }
            }
        } catch (IOException e) {
            Logger.w(e);

        } finally {
            InternalUtils.closeQuietly(printWriter);
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e) {
                    Logger.w(e);
                }
            }
            InternalUtils.closeQuietly(lockChannel);
            InternalUtils.closeQuietly(lockRaf);
        }
    }

    @WorkerThread
    public void writeToFile(LogMessage logMessage, File file) {
        if (!file.exists()) {
            Logger.w("Log file not exist, can not write!");
            return;
        }
        RandomAccessFile lockRaf = null;
        FileChannel lockChannel = null;
        FileLock fileLock = null;
        PrintWriter printWriter = null;

        try {
            lockRaf = new RandomAccessFile(file, "rw");
            lockChannel = lockRaf.getChannel();
            fileLock = lockChannel.lock();

            synchronized (mLock) {
                FileOutputStream fos = new FileOutputStream(file, true);
                OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
                printWriter = new PrintWriter(writer);
                printWriter.println(logMessage.buildMessage(mFormatter));
            }
        } catch (IOException e) {
            Logger.w(e);

        } finally {
            InternalUtils.closeQuietly(printWriter);
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e) {
                    Logger.w(e);
                }
            }
            InternalUtils.closeQuietly(lockChannel);
            InternalUtils.closeQuietly(lockRaf);
        }
    }

    @WorkerThread
    public void cleanExpiredLogs() {
        File folder = mSetting.getLogDirectory();
        if (folder == null) {
            Logger.w("Log directory is null.");
            return;
        }

        if (folder.exists() && folder.isDirectory()) {
            File[] allFiles = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String fileName) {
                    return fileName.contains(FILE_HYPHEN) &&
                            (fileName.endsWith(LOG_FILE_EXTENSION)
                                    || fileName.endsWith(EVENT_FILE_EXTENSION)
                                    || fileName.endsWith(ZIP_FILE_EXTENSION));
                }
            });

            for (File file : allFiles) {
                String fileName = file.getName();
                String fileDateInfo = getFileNameWithoutExtension(fileName);

                if (fileDateInfo == null) {
                    continue;
                }

                if (isExpired(fileDateInfo)) {
                    InternalUtils.delete(file);
                }
            }
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int index = fileName.indexOf(FILE_HYPHEN);
        if (index == -1) return null;

        return fileName.substring(0, index);
    }

    private boolean isExpired(String dateStr) {
        boolean canDel;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * mSetting.getExpiredDay());
        Date expiredDate = calendar.getTime();
        try {
            Date createDate = mNameFormatter.parse(dateStr);
            canDel = createDate.before(expiredDate);
        } catch (ParseException e) {
            canDel = false;
        }
        return canDel;
    }

    public File[] queryFilesByDate(final int mode, long ms) {
        File folder = mSetting.getLogDirectory();
        if (folder == null) {
            Logger.w("Log directory is null.");
            return null;
        }

        final String name = mNameFormatter.format(new Date(ms));
        if (folder.exists() && folder.isDirectory()) {
            return folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    switch (mode) {
                        case LogSetting.EVENT:
                            return filename.startsWith(name) && filename.contains(FILE_HYPHEN)
                                    && filename.endsWith(EVENT_FILE_EXTENSION);
                        case LogSetting.LOG:
                            return filename.startsWith(name) && filename.contains(FILE_HYPHEN)
                                    && filename.endsWith(LOG_FILE_EXTENSION);
                        case LogSetting.LOG | LogSetting.EVENT:
                        default:
                            return filename.startsWith(name) && filename.contains(FILE_HYPHEN)
                                    && (filename.endsWith(LOG_FILE_EXTENSION)
                                    || filename.endsWith(EVENT_FILE_EXTENSION));
                    }
                }
            });

        }
        return null;
    }

    public File[] queryFiles(final int mode) {
        File folder = mSetting.getLogDirectory();
        if (folder == null) {
            Logger.w("Log directory is null.");
            return null;
        }

        if (folder.exists() && folder.isDirectory()) {
            return folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    switch (mode) {
                        case LogSetting.EVENT:
                            return filename.contains(FILE_HYPHEN)
                                    && filename.endsWith(EVENT_FILE_EXTENSION);
                        case LogSetting.LOG:
                            return filename.contains(FILE_HYPHEN)
                                    && filename.endsWith(LOG_FILE_EXTENSION);
                        case LogSetting.LOG | LogSetting.EVENT:
                        default:
                            return filename.contains(FILE_HYPHEN)
                                    && (filename.endsWith(LOG_FILE_EXTENSION)
                                    || filename.endsWith(EVENT_FILE_EXTENSION));
                    }
                }
            });

        }
        return null;
    }


    /**
     * Message Entity
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class LogMessage {
        private static final Pools.SynchronizedPool<LogMessage> sPool =
                new Pools.SynchronizedPool<>(20);
        public int priority;
        public long time;
        public String tag;
        public String msg;
        public String thread;

        public LogMessage() {

        }

        public static LogMessage obtain() {
            LogMessage instance = sPool.acquire();
            return (instance != null) ? instance : new LogMessage();
        }

        public void setMessage(int priority, long time, String tag, String thread, String msg) {
            this.priority = priority;
            this.time = time;
            this.tag = tag;
            this.thread = thread;
            this.msg = msg;
        }

        public String buildMessage(LogFormatter formatter) {
            return formatter.buildMessage(priority, time, tag, thread, msg);
        }

        public void recycle() {
            sPool.release(this);
        }
    }
}
