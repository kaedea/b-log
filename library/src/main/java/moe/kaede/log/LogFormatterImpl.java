/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author kaede
 * @version date 16/9/25
 */

class LogFormatterImpl implements LogFormatter {

    private final boolean mShowThreadInfo;
    private final SimpleDateFormat mDateFormatter;

    public LogFormatterImpl() {
        mShowThreadInfo = false;
        mDateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());
    }

    public LogFormatterImpl(LogSetting setting) {
        mShowThreadInfo = setting.isShowThreadInfo();
        mDateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());
    }

    @NonNull
    @Override
    public String emptyMessage() {
        return "Empty/NULL";
    }

    @NonNull
    @Override
    public String buildMessage(int logType, long time, String tag, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(mDateFormatter.format(time))
                .append("  ")
                .append(LogLevel.getLevelName(logType))
                .append("/")
                .append(tag)
                .append("  ");

        if (mShowThreadInfo) {
            sb.append("[")
                    .append(Thread.currentThread().getName())
                    .append("]  ")
                    .append(msg);
        } else {
            sb.append("  ")
                    .append(msg);
        }



        return sb.toString();
    }
}
