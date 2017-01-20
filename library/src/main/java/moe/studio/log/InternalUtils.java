/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.os.Process;
import android.text.TextUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kaede
 * @version date 16/9/23
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class InternalUtils {

    @SuppressWarnings("SpellCheckingInspection")
    static String getProcessName() {
        int pid = Process.myPid();
        StringBuilder cmdline = new StringBuilder();
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

    static boolean exist(String path) {
        return !TextUtils.isEmpty(path) && (new File(path).exists());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void checkCreateFile(File file) throws IOException {
        if (file == null) {
            throw new IOException("File is null.");
        }
        if (file.exists()) {
            if (!file.isDirectory()) {
                return;
            }
            delete(file);
        }
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!file.createNewFile()) {
            throw new IOException("Create file fail, file already exists.");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void checkCreateDir(File file) throws IOException {
        if (file == null) {
            throw new IOException("Dir is null.");
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                return;
            }
            if (!delete(file)) {
                throw new IOException("Fail to delete existing file, file = "
                        + file.getAbsolutePath());
            }
            file.mkdir();
        } else {
            file.mkdirs();
        }
        if (!file.exists() || !file.isDirectory()) {
            throw new IOException("Fail to create dir, dir = " + file.getAbsolutePath());
        }
    }

    static boolean delete(File file) {
        return FileUtils.deleteQuietly(file);
    }

    static void closeQuietly(Closeable closeable) {
        IOUtils.closeQuietly(closeable);
    }

    static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    static boolean zippingFiles(List<File> files, File output) {
        if (files == null || files.size() == 0) return false;

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
            Logger.w(e);
            return false;

        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }
}
