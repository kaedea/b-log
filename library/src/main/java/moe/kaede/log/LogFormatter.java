/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

/**
 * Log Message formatter
 *
 * @author kaede
 * @version date 16/9/25
 */

public interface LogFormatter {
    String emptyMessage();

    String buildMessage(int logType, long time, String tag, String thread, String msg);
}
