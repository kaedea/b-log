/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com)
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

    String buildMessage(int priority, long time, String tag, String thread, String msg);
}
