/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * @author kaede
 * @version date 16/9/22
 */

class Executor {

    private static Executor sInstance = new Executor();
    private Handler mHandler;

    private Executor() {
    }

    public static Executor instance() {
        return sInstance;
    }

    private void ensureHandler() {
        if (mHandler == null) {
            synchronized (Executor.this) {
                if (mHandler == null) {
                    HandlerThread thread = new HandlerThread("thread_blog_io");
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                    mHandler = new Handler(thread.getLooper());
                }
            }
        }
    }

    public void post(Runnable runnable) {
        if (runnable == null) return;

        ensureHandler();
        mHandler.post(runnable);
    }

    public boolean hasMessages(int what) {
        ensureHandler();
        return mHandler.hasMessages(what);
    }

    public void postMessage(int what, Runnable runnable) {
        if (runnable == null) return;

        ensureHandler();
        Message message = Message.obtain(mHandler, runnable);
        message.what = what;
        mHandler.sendMessageDelayed(message, 2000);

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
}
