/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import moe.studio.dispatcher.Task;

/**
 * @author kaede
 * @version date 16/9/22
 */

@SuppressWarnings("WeakerAccess")
class Executor {

    private static final int DELAY_MILLIS = 2000;
    private static Task.Dispatcher sDispatcher;

    private static void ensureHandler() {
        if (sDispatcher == null) {
            synchronized (Executor.class) {
                if (sDispatcher == null) {
                    sDispatcher = Task.Dispatchers.newSimpleDispatcher();
                    sDispatcher.start();
                }
            }
        }
    }

    public static void post(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        ensureHandler();
        sDispatcher.post(runnable);
    }

    public static void post(int what, Runnable runnable) {
        if (runnable == null) {
            return;
        }
        ensureHandler();
        sDispatcher.postDelay(what, runnable, DELAY_MILLIS);

    }

    public static boolean has(int what) {
        ensureHandler();
        return sDispatcher.has(what);
    }

    public static void setDispatcher(Task.Dispatcher dispatcher) {
        if (dispatcher != null) {
            sDispatcher = dispatcher;
        }
    }
}
