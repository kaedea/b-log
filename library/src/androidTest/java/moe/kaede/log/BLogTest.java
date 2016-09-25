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

    public void testInitializeWithConfig() {
        Context context = getInstrumentation().getTargetContext();
        try {
            File logDir = context.getExternalFilesDir("test_log");
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

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        BLog.shutdown();
    }

    public void testCleanExpiredFiles() {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());
        BLog.deleteLogs();
        assertEquals(folder.exists(), false);

        folder.mkdirs();
        assertEquals(folder.listFiles().length, 0);

        try {
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

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        BLog.shutdown();
    }

    public void testExecutor() {
        final int[] i = {0};
        final Looper looper = Looper.myLooper();
        Executor.instance().post(new Runnable() {
            @Override
            public void run() {
                assertTrue(Looper.myLooper() != looper);
                i[0]++;
            }
        });
        try {
            Thread.sleep(100);
            assertEquals(i[0], 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        BLog.shutdown();
    }


    public void testLogCount() {
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
        try {
            Thread.sleep(3000);

            assertTrue(log.exists());
            assertTrue(event.exists());

            lineNumberReader1 = new LineNumberReader(new FileReader(log));
            lineNumberReader1.skip(Long.MAX_VALUE);
            assertEquals(lineNumberReader1.getLineNumber(), 45);

            lineNumberReader2 = new LineNumberReader(new FileReader(event));
            lineNumberReader2.skip(Long.MAX_VALUE);
            assertEquals(lineNumberReader2.getLineNumber(), 120);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            assertTrue(false);

        } finally {
           InternalUtils.closeQuietly(lineNumberReader1);
           InternalUtils.closeQuietly(lineNumberReader2);
        }

        BLog.shutdown();
    }

    public void testPrintStackTrace() {
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
        try {
            Thread.sleep(3000);

            lineNumberReader = new LineNumberReader(new FileReader(log));
            lineNumberReader.skip(Long.MAX_VALUE);
            assertTrue(lineNumberReader.getLineNumber() > 1); // may have 15 lines

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            assertTrue(false);

        } finally {
            InternalUtils.closeQuietly(lineNumberReader);
        }

        BLog.shutdown();
    }

    public void testMultiThread() {
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
        try {
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

            Thread.sleep(10000);

            ll = new LineNumberReader(new FileReader(log));
            ll.skip(Long.MAX_VALUE);
            assertEquals(ll.getLineNumber(), 2000);

            le = new LineNumberReader(new FileReader(event));
            le.skip(Long.MAX_VALUE);
            assertEquals(le.getLineNumber(), 2000);

        } catch (IOException | InterruptedException t) {
        	t.printStackTrace();
            assertTrue(false);
        }

        BLog.shutdown();
    }

    public void testQueryFiles() {
        Context context = getInstrumentation().getTargetContext();
        BLog.initialize(context);
        LogSetting setting = BLog.getSetting();

        File folder = new File(setting.getLogDir());
        assertTrue(folder.exists());
        BLog.deleteLogs();

        BLog.d("TEST", "传授一点人生经验");
        BLog.event("TEST", "Exciting!");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        File[] files = BLog.getFilesByDate(new Date());

        assertNotNull(files);
        assertEquals(2, files.length);

        BLog.shutdown();
    }
}
