/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.support.annotation.NonNull;

/**
 * Log Message formatter
 *
 * @author kaede
 * @version date 16/9/25
 */

public interface LogFormatter {
    @NonNull
    String emptyMessage();

    @NonNull
    String buildMessage(int logType, long time, String tag, String msg);
}
