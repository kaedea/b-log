/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com)>
 */

package moe.studio.log;

interface Log {
    /**
     * Log a message.
     *
     * @param priority The current log level.
     * @param tag      Log tag.
     * @param msg      Log message content.
     */
    void log(int priority, String tag, String msg);

    void onShutdown();
}
