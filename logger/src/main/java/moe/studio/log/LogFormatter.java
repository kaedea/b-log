/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

/**
 * Log Message formatter
 *
 * @author kaede
 * @version date 16/9/25
 */

public interface LogFormatter {

    /**
     * Get empty log message style.
     */
    String emptyMessage();

    /**
     * Build a log message to log in file.
     */
    String buildMessage(int priority, long time, String tag, String thread, String msg);
}
