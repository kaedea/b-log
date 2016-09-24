/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.annotation.WorkerThread;

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
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String EVENT_FILE_EXTENSION = ".event";
    private static final String FILE_HYPHEN = "-";

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

    private final LogSetting mSetting;
    private final SimpleDateFormat mNameFormatter = new SimpleDateFormat(
            "yyyyMMdd", Locale.getDefault());

    private Files(LogSetting setting) {
        mSetting = setting;
    }

    /**
     * @return ROOT_DIR/Date-ProcessMame.log
     */
    public String getLogPath() {
        String date = mNameFormatter.format(System.currentTimeMillis());
        return InternalUtils.ensureSeparator(mSetting.getLogDir()) + date + FILE_HYPHEN + InternalUtils.getProcessName()
                + LOG_FILE_EXTENSION;
    }

    /**
     * @return ROOT_DIR/Date-ProcessMame.event
     */
    public String getEventPath() {
        String date = mNameFormatter.format(System.currentTimeMillis());
        return InternalUtils.ensureSeparator(mSetting.getLogDir()) + date + FILE_HYPHEN + InternalUtils.getProcessName()
                + EVENT_FILE_EXTENSION;
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
                if (LogSetting.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        return file.exists() && file.canWrite();
    }

    @WorkerThread
    public void writeToFile(List<String> strs, String filePath) {
        PrintWriter printWriter = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) return;

            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
            printWriter = new PrintWriter(writer);

            for (String str : strs) {
                printWriter.println(str);
            }

        } catch (IOException e) {
            if (LogSetting.DEBUG) {
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
                            (fileName.endsWith(LOG_FILE_EXTENSION) || fileName.endsWith(EVENT_FILE_EXTENSION));
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

    public File[] queryFilesByDate(long ms) {
        final String name = mNameFormatter.format(new Date(ms));
        File folder = new File(mSetting.getLogDir());

        if (folder.exists() && folder.isDirectory()) {
            File[] allFiles = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(name) && filename.contains(FILE_HYPHEN)
                            && (filename.endsWith(LOG_FILE_EXTENSION) || filename.endsWith(EVENT_FILE_EXTENSION));
                }
            });
            return allFiles;

        }
        return null;
    }

}
