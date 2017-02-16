/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

/**
 * @author Kaede
 * @since 2017/1/20
 */
@SuppressWarnings("WeakerAccess")
class Logger {

    static final String TAG = "BLog";

    public static void w(String message) {
        android.util.Log.w(TAG, message);
    }

    public static void w(Throwable throwable) {
        android.util.Log.w(TAG, throwable);
    }

    public static void w(String message, Throwable throwable) {
        android.util.Log.w(TAG, message, throwable);
    }
}
