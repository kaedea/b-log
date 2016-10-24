/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.annotation.WorkerThread;
import android.support.v4.util.Pools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    /**
     * @return ROOT_DIR/20160927-main.log
     */
    public String getLogPath() {
        String date = mNameFormatter.format(System.currentTimeMillis());
        return InternalUtils.ensureSeparator(mSetting.getLogDir()) + date + FILE_HYPHEN + InternalUtils.getProcessName()
                + LOG_FILE_EXTENSION;
    }

    /**
     * @return ROOT_DIR/20160927-main.event
     */
    public String getEventPath() {
        String date = mNameFormatter.format(System.currentTimeMillis());
        return InternalUtils.ensureSeparator(mSetting.getLogDir()) + date + FILE_HYPHEN + InternalUtils.getProcessName()
                + EVENT_FILE_EXTENSION;
    }

    /**
     * @return ROOT_DIR/20160927-all.zip
     */
    public String getZipPath(int mode) {
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
        return InternalUtils.ensureSeparator(mSetting.getLogDir()) + date + FILE_HYPHEN + suffix
                + ZIP_FILE_EXTENSION;
    }

    public boolean canWrite(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) InternalUtils.deleteQuietly(file);

        } else {
            try {
                File parentFile = file.getParentFile();
                parentFile.mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                if (mSetting.debuggable()) {
                    e.printStackTrace();
                }
            }
        }

        return file.exists() && file.canWrite();
    }

    @WorkerThread
    public synchronized void writeToFile(List<LogMessage> logMessages, String filePath) {
        PrintWriter printWriter = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) return;

            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
            printWriter = new PrintWriter(writer);

            for (LogMessage logMessage : logMessages) {
                printWriter.println(logMessage.buildMessage(mFormatter));
            }

        } catch (IOException e) {
            if (mSetting.debuggable()) {
                e.printStackTrace();
            }

        } finally {
            InternalUtils.closeQuietly(printWriter);
        }
    }

    @WorkerThread
    public void cleanExpiredLogs() {
        File folder = new File(mSetting.getLogDir());
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
                    InternalUtils.deleteQuietly(file);
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
        final String name = mNameFormatter.format(new Date(ms));
        File folder = new File(mSetting.getLogDir());

        if (folder.exists() && folder.isDirectory()) {
            File[] allFiles = folder.listFiles(new FilenameFilter() {
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
            return allFiles;

        }
        return null;
    }

    public File[] queryFiles(final int mode) {
        File folder = new File(mSetting.getLogDir());

        if (folder.exists() && folder.isDirectory()) {
            File[] allFiles = folder.listFiles(new FilenameFilter() {
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
            return allFiles;

        }
        return null;
    }


    /**
     * Message Entity
     */
    public static class LogMessage {
        private static final Pools.SynchronizedPool<LogMessage> sPool =
                new Pools.SynchronizedPool(20);
        public int logType;
        public long time;
        public String tag;
        public String msg;

        public LogMessage() {

        }

        public static LogMessage obtain() {
            LogMessage instance = sPool.acquire();
            return (instance != null) ? instance : new LogMessage();
        }

        public void setMessage(int logType, long time, String tag, String msg) {
            this.logType = logType;
            this.time = time;
            this.tag = tag;
            this.msg = msg;
        }

        public String buildMessage(LogFormatter formatter) {
            return formatter.buildMessage(logType, time, tag, msg);
        }

        public void recycle() {
            sPool.release(this);
        }
    }
}
