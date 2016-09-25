/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.os.Process;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kaede
 * @version date 16/9/23
 */
class InternalUtils {

    static String getProcessName() {
        int pid = Process.myPid();
        StringBuffer cmdline = new StringBuffer();
        InputStream is = null;
        try {
            is = new FileInputStream("/proc/" + pid + "/cmdline");
            for (; ; ) {
                int c = is.read();
                if (c < 0)
                    break;
                cmdline.append((char) c);
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        String pname = cmdline.toString().trim();
        if (!pname.contains(":")) {
            return "main";
        } else {
            return pname.substring(pname.indexOf(":") + 1);
        }
    }

    public static void createDir(File dir) {
        if (!dir.isDirectory()) {
            dir.delete();
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    static String ensureSeparator(String path) {
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            return path;
        }
        return path + File.separator;
    }

    static void deleteQuietly(File file) {
        if (file == null || !file.exists()) return;

        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        delelteDirectory(file);
    }

    static void delelteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                delelteDirectory(file);
            }
        }
        directory.delete();
    }

    static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignore) {
        }
    }
}
