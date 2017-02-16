/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BLogTest extends InstrumentationTestCase {

    public void testInitialize() {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        assertNotNull(setting);
        assertTrue(BLog.getLogDir().exists());
        assertTrue(setting.getExpiredDay() == 2);

        BLog.shutdown();
    }

    public void testInitializeWithConfig() throws IOException {
        Context context = getInstrumentation().getTargetContext();
        File logDir = context.getExternalFilesDir("test_log");
        if (logDir == null)
            logDir = context.getDir("test_log", Context.MODE_PRIVATE);

        logDir.createNewFile();

        LogSetting setting = new LogSetting.Builder(context)
                .setDefaultTag("TEST")
                .setLogDir(logDir.getPath())
                .setExpiredDay(1)
                .setLogcatPriority(LogPriority.DEBUG)
                .setLogfilePriority(LogPriority.INFO)
                .setEventPriority(LogPriority.VERBOSE)
                .setFormatter(new LogFormatterImpl())
                .build();

        BLog.initialize(setting);
        setting = BLog.getSetting();

        assertNotNull(setting);
        assertTrue(new File(setting.getLogDir()).exists());
        assertTrue(setting.getLogDir().equals(logDir.getAbsolutePath()));
        assertTrue(setting.getDefaultTag().equals("TEST"));
        assertTrue(setting.getExpiredDay() == 1);
        assertTrue(setting.getLogcatPriority() == LogPriority.DEBUG);
        assertTrue(setting.getLogfilePriority() == LogPriority.INFO);
        assertTrue(setting.getLogFormatter() instanceof LogFormatterImpl);

        BLog.shutdown();
    }

    public void testCleanExpiredFiles() throws IOException, InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());
        BLog.deleteLogs();
        assertEquals(folder.exists(), false);

        folder.mkdirs();
        assertEquals(folder.listFiles().length, 0);

        File outDate1 = new File(folder.getAbsolutePath() + File.separator + "20160520-main.log");
        File outDate2 = new File(folder.getAbsolutePath() + File.separator + "20140516-downlaod.log");
        File outDate3 = new File(folder.getAbsolutePath() + File.separator + "20150602-web.event");
        File outDate4 = new File(folder.getAbsolutePath() + File.separator + "20130322-main.event");
        outDate1.createNewFile();
        outDate2.createNewFile();
        outDate3.createNewFile();
        outDate4.createNewFile();

        File newLog = new File(Files.instance(setting).getLogFile().getAbsolutePath());
        newLog.createNewFile();

        assertEquals(folder.listFiles().length, 5);

        LogEngine logEngine = BLog.getLogger();
        logEngine.cleanExpiredFiles();

        Thread.sleep(30);

        assertEquals(folder.listFiles().length, 1);

        BLog.shutdown();
    }

    public void testExecutor() throws InterruptedException {
        final int[] i = {0};
        final Looper looper = Looper.myLooper();
        Executor.post(new Runnable() {
            @Override
            public void run() {
                assertTrue(Looper.myLooper() != looper);
                i[0]++;
            }
        });
        Thread.sleep(100);
        assertEquals(i[0], 1);
    }


    public void testLogCount() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        File log = Files.instance(setting).getLogFile();
        File event = Files.instance(setting).getEventFile();
        BLog.deleteLogs();

        int i = 0;
        while (i < 120) {
            BLog.event("TEST", "naive! " + i);

            if (i < 45) {
                BLog.d("TEST", "too young! " + i);
            }

            i++;
        }

        LineNumberReader lineNumberReader1 = null;
        LineNumberReader lineNumberReader2 = null;
        Thread.sleep(5000);

        assertTrue(log.exists());
        assertTrue(event.exists());

        lineNumberReader1 = new LineNumberReader(new FileReader(log));
        lineNumberReader1.skip(Long.MAX_VALUE);
        assertEquals(lineNumberReader1.getLineNumber(), 45);

        lineNumberReader2 = new LineNumberReader(new FileReader(event));
        lineNumberReader2.skip(Long.MAX_VALUE);
        assertEquals(lineNumberReader2.getLineNumber(), 120);
        InternalUtils.closeQuietly(lineNumberReader1);
        InternalUtils.closeQuietly(lineNumberReader2);

        BLog.shutdown();
    }

    public void testPrintStackTrace() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        File log = new File(Files.instance(setting).getLogFile().getAbsolutePath());
        BLog.deleteLogs();

        try {
            String e = null;
            // throw null exception
            boolean b = e.length() == 3;
        } catch (Exception e) {
            //print stacktrace, probably has 15 lines
            BLog.w("TEST", null, e);
        }

        LineNumberReader lineNumberReader = null;
        Thread.sleep(3000);

        lineNumberReader = new LineNumberReader(new FileReader(log));
        lineNumberReader.skip(Long.MAX_VALUE);
        assertTrue(lineNumberReader.getLineNumber() > 1); // may have 15 lines
        InternalUtils.closeQuietly(lineNumberReader);

        BLog.shutdown();
    }

    public void testMultiThread() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        File log = new File(Files.instance(setting).getLogFile().getAbsolutePath());
        File event = new File(Files.instance(setting).getEventFile().getAbsolutePath());
        BLog.deleteLogs();

        LineNumberReader ll = null;
        LineNumberReader le = null;
        final CountDownLatch countDownLatch = new CountDownLatch(4);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 1000) {
                    BLog.d("thread 1", "做了一点小成绩");
                    i++;
                }
                countDownLatch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 1000) {
                    BLog.d("thread 2", "无可奉告");
                    i++;
                }
                countDownLatch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 1000) {
                    BLog.event("thread 3", "成天就想搞个大新闻");
                    i++;
                }
                countDownLatch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 1000) {
                    BLog.event("thread 4", "谈笑风生");
                    i++;
                }
                countDownLatch.countDown();
            }
        }).start();

        long current = SystemClock.elapsedRealtime();
        countDownLatch.await();

        // logcat task should be very fast
        long interval = SystemClock.elapsedRealtime() - current;

        assertTrue(interval < 1000);

        Thread.sleep(15000);

        ll = new LineNumberReader(new FileReader(log));
        ll.skip(Long.MAX_VALUE);
        assertEquals(ll.getLineNumber(), 2000);

        le = new LineNumberReader(new FileReader(event));
        le.skip(Long.MAX_VALUE);
        assertEquals(le.getLineNumber(), 2000);


        String path = Files.instance(setting).getZipFile(LogSetting.LOG | LogSetting.EVENT).getAbsolutePath();
        File[] files = BLog.getLogFilesByDate(LogSetting.LOG | LogSetting.EVENT, null);
        File output = new File(path);

        assertTrue(!output.exists());
        assertTrue(InternalUtils.zippingFiles(Arrays.asList(files), output));
        assertTrue(output.exists());

        BLog.shutdown();
    }

    public void testGetLogFiles() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        LogSetting setting = new LogSetting.Builder(context)
                .showThreadInfo(true)
                .build();
        BLog.initialize(setting);

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());
        BLog.deleteLogs();

        BLog.d("TEST", "传授一点人生经验");
        BLog.event("TEST", "Exciting!");

        Thread.sleep(3000);

        File[] files = BLog.getLogFilesByDate(LogSetting.LOG | LogSetting.EVENT, null);
        assertNotNull(files);
        assertEquals(2, files.length);

        files = BLog.getLogFilesByDate(LogSetting.LOG, null);
        assertNotNull(files);
        assertEquals(1, files.length);
        assertTrue(files[0].getAbsolutePath().contains(Files.LOG_FILE_EXTENSION));

        files = BLog.getLogFilesByDate(LogSetting.EVENT, null);
        assertNotNull(files);
        assertEquals(1, files.length);
        assertTrue(files[0].getAbsolutePath().contains(Files.EVENT_FILE_EXTENSION));

        File outDate1 = new File(folder.getAbsolutePath() + File.separator + "20160520-main.log");
        File outDate3 = new File(folder.getAbsolutePath() + File.separator + "20150602-web.event");
        File outDate4 = new File(folder.getAbsolutePath() + File.separator + "20130322-main.event");
        outDate1.createNewFile();
        outDate3.createNewFile();
        outDate4.createNewFile();

        files = BLog.getLogFiles(LogSetting.LOG | LogSetting.EVENT);
        assertNotNull(files);
        assertEquals(5, files.length);

        files = BLog.getLogFiles(LogSetting.LOG);
        assertNotNull(files);
        assertEquals(2, files.length);

        files = BLog.getLogFiles(LogSetting.EVENT);
        assertNotNull(files);
        assertEquals(3, files.length);

        BLog.shutdown();
    }

    public void testZippingLogFiles() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        LogSetting setting = new LogSetting.Builder(context)
                .showThreadInfo(true)
                .build();
        BLog.initialize(setting);

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());
        BLog.deleteLogs();

        BLog.d("TEST", "传授一点人生经验");
        BLog.event("TEST", "Exciting!");

        Thread.sleep(3000);

        String path;
        path = Files.instance(setting).getZipFile(LogSetting.EVENT).getAbsolutePath();
        assertTrue(path.contains("event"));
        path = Files.instance(setting).getZipFile(LogSetting.EVENT | LogSetting.LOG).getAbsolutePath();
        assertTrue(path.contains("all"));
        path = Files.instance(setting).getZipFile(LogSetting.LOG).getAbsolutePath();
        assertTrue(path.contains("log"));

        File[] files = BLog.getLogFilesByDate(LogSetting.LOG, null);
        File output = new File(path);

        assertTrue(!output.exists());
        assertTrue(InternalUtils.zippingFiles(Arrays.asList(files), output));
        assertTrue(output.exists());

        File all = BLog.zippingLogFiles(LogSetting.LOG | LogSetting.EVENT, null);
        assertTrue(all.exists());
        int allCount = 0;
        try {
            ZipFile zipFile = new ZipFile(all);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    allCount ++;
                }
            }
        } catch (IOException e) {
            assertNull(e);
        }

        File event = BLog.zippingLogFilesByDate(LogSetting.EVENT, new Date(), null);
        assertTrue(event.exists());
        assertTrue(all.length() > event.length());
        int eventCount = 0;
        try {
            ZipFile zipFile = new ZipFile(event);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    eventCount ++;
                }
            }
        } catch (IOException e) {
            assertNull(e);
        }

        assertTrue(eventCount < allCount);

        File outDate1 = new File(folder.getAbsolutePath() + File.separator + "attatch1");
        File outDate3 = new File(folder.getAbsolutePath() + File.separator + "attatch2");
        File outDate4 = new File(folder.getAbsolutePath() + File.separator + "attatch3");
        outDate1.createNewFile();
        outDate3.createNewFile();
        outDate4.createNewFile();
        List<File> attaches = new ArrayList<>();
        attaches.add(outDate1);
        attaches.add(outDate3);
        attaches.add(outDate4);

        File attach = BLog.zippingLogFiles(LogSetting.LOG | LogSetting.EVENT, attaches);
        assertTrue(attach.exists());
        assertTrue(all.length() > event.length());
        int allAttach = 0;
        try {
            ZipFile zipFile = new ZipFile(attach);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    allAttach ++;
                }
            }
        } catch (IOException e) {
            assertNull(e);
        }

        assertEquals(allAttach, allCount + attaches.size());
    }

    public void testSyncLogFiles() throws IOException, InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        File log = new File(Files.instance(setting).getLogFile().getAbsolutePath());
        BLog.deleteLogs();

        Runnable runnable = new Runnable() {
            public void run() {
                int i = 0;
                while (i < 120) {
                    BLog.syncLog(LogPriority.INFO, "TEST", "你们这要搞事");

                    if (i < 20) {
                        BLog.syncLog(LogPriority.VERBOSE, "TEST", "要是将来报道出了偏差");
                    }

                    if (i < 30) {
                        BLog.syncLog(LogPriority.DEBUG, "TEST", "可是要负责的");
                    }

                    i++;
                }
            }
        };

        Executor.post(runnable);

        Thread.sleep(3000);

        LineNumberReader lineNumberReader = null;
        assertTrue(log.exists());

        lineNumberReader = new LineNumberReader(new FileReader(log));
        lineNumberReader.skip(Long.MAX_VALUE);
        assertEquals(lineNumberReader.getLineNumber(), 120 + 30);

        InternalUtils.closeQuietly(lineNumberReader);
        BLog.shutdown();
    }

    public void testLogAdapter() throws InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        final int[] logCount = new int[1];
        final boolean[] isShutdown = new boolean[1];

        LogSetting setting = new LogSetting.Builder(context)
                .setAdapter(new Log() {
                    @Override
                    public void log(int priority, String tag, String msg) {
                        logCount[0]++;
                    }

                    @Override
                    public void onShutdown() {
                        isShutdown[0] = true;
                    }
                })
                .build();

        BLog.initialize(setting);

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        int i = 0;
        while (i < 120) {
            BLog.event("TEST", "naive! " + i);

            if (i < 45) {
                BLog.d("TEST", "too young! " + i);
            }

            i++;
        }

        Thread.sleep(5000);
        assertEquals(logCount[0], 45 + 120);
        assertEquals(isShutdown[0], false);

        BLog.shutdown();
        assertEquals(isShutdown[0], true);
    }
}
