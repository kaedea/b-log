/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.os.Process;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        } catch (Exception ignore) {
        } finally {
            closeQuietly(is);
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
        FileUtils.deleteQuietly(file);
    }

    static void closeQuietly(Closeable closeable) {
        IOUtils.closeQuietly(closeable);
    }

    static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    static boolean zippingFiles(File[] files, File output) {
        if (files == null || files.length == 0) return false;

        BufferedInputStream in = null;
        ZipOutputStream out = null;
        try {
            FileOutputStream fo = new FileOutputStream(output);
            out = new ZipOutputStream(new BufferedOutputStream(fo));
            out.setLevel(Deflater.BEST_COMPRESSION); // best compress

            for (File file : files) {
                FileInputStream fi = new FileInputStream(file);
                in = new BufferedInputStream(fi, 2048);
                String entryName = file.getName();
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                IOUtils.copy(in, out);
                in.close();
            }
            return true;

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return false;

        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }
}
