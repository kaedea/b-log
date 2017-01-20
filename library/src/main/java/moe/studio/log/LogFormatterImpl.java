/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author kaede
 * @version date 16/9/25
 */

@SuppressWarnings("WeakerAccess")
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

    @Override
    public String emptyMessage() {
        return "Empty/NULL";
    }

    @Override
    public String buildMessage(int priority, long time, String tag, String thread, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(mDateFormatter.format(time))
                .append("  ")
                .append(LogPriority.getName(priority))
                .append("/")
                .append(tag)
                .append("  ");

        if (mShowThreadInfo) {
            sb.append("[")
                    .append(thread)
                    .append("]  ")
                    .append(msg);
        } else {
            sb.append("  ")
                    .append(msg);
        }

        return sb.toString();
    }
}
