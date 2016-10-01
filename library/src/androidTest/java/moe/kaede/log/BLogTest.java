/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

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
                .setLogcatLevel(LogLevel.DEBUG)
                .setLogfileLevel(LogLevel.INFO)
                .setEventLevel(LogLevel.VERBOSE)
                .setFormatter(new LogFormatterImpl())
                .build();

        BLog.initialize(setting);
        setting = BLog.getSetting();

        assertNotNull(setting);
        assertTrue(new File(setting.getLogDir()).exists());
        assertTrue(setting.getLogDir().equals(logDir.getAbsolutePath()));
        assertTrue(setting.getDefaultTag().equals("TEST"));
        assertTrue(setting.getExpiredDay() == 1);
        assertTrue(setting.getLogcatLevel() == LogLevel.DEBUG);
        assertTrue(setting.getLogfileLevel() == LogLevel.INFO);
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

        File newLog = new File(Files.instance(setting).getLogPath());
        newLog.createNewFile();

        assertEquals(folder.listFiles().length, 5);

        Logger logger = BLog.getLogger();
        logger.cleanExpiredFiles();

        Thread.sleep(30);

        assertEquals(folder.listFiles().length, 1);

        BLog.shutdown();
    }

    public void testExecutor() throws InterruptedException {
        final int[] i = {0};
        final Looper looper = Looper.myLooper();
        Executor.instance().post(new Runnable() {
            @Override
            public void run() {
                assertTrue(Looper.myLooper() != looper);
                i[0]++;
            }
        });
        Thread.sleep(100);
        assertEquals(i[0], 1);

        BLog.shutdown();
    }


    public void testLogCount() throws InterruptedException, IOException {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());

        File log = new File(Files.instance(setting).getLogPath());
        File event = new File(Files.instance(setting).getEventPath());
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

        File log = new File(Files.instance(setting).getLogPath());
        BLog.deleteLogs();

        try {
            String e = null;
            // throw null exception
            boolean b = e.length() == 3;
        } catch (Exception e) {
            //print stacktrace, probably has 15 lines
            BLog.w("TEST", e, null);
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

        File log = new File(Files.instance(setting).getLogPath());
        File event = new File(Files.instance(setting).getEventPath());
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


        String path = Files.instance(setting).getZipPath(LogSetting.LOG | LogSetting.EVENT);
        File[] files = BLog.geLogFilesByDate(LogSetting.LOG | LogSetting.EVENT, null);
        File output = new File(path);

        assertTrue(!output.exists());
        assertTrue(InternalUtils.zippingFiles(files, output));
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

        File[] files = BLog.geLogFilesByDate(LogSetting.LOG | LogSetting.EVENT, null);
        assertNotNull(files);
        assertEquals(2, files.length);

        files = BLog.geLogFilesByDate(LogSetting.LOG, null);
        assertNotNull(files);
        assertEquals(1, files.length);
        assertTrue(files[0].getAbsolutePath().contains(Files.LOG_FILE_EXTENSION));

        files = BLog.geLogFilesByDate(LogSetting.EVENT, null);
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

    public void testZippingLogFiles() throws InterruptedException {
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
        path = Files.instance(setting).getZipPath(LogSetting.EVENT);
        assertTrue(path.contains("event"));
        path = Files.instance(setting).getZipPath(LogSetting.EVENT | LogSetting.LOG);
        assertTrue(path.contains("all"));
        path = Files.instance(setting).getZipPath(LogSetting.LOG);
        assertTrue(path.contains("log"));

        File[] files = BLog.geLogFilesByDate(LogSetting.LOG, null);
        File output = new File(path);

        assertTrue(!output.exists());
        assertTrue(InternalUtils.zippingFiles(files, output));
        assertTrue(output.exists());

        File all = BLog.zippingLogFiles(LogSetting.LOG | LogSetting.EVENT);
        assertTrue(all.exists());
        File event = BLog.zippingLogFilesByDate(LogSetting.EVENT, new Date());
        assertTrue(event.exists());
        assertTrue(all.length() > event.length());
    }
}
