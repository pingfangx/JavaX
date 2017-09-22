package com.pingfangx.tools.base;

/**
 * 日志记录器
 *
 */
public interface ILogger {
    /**
     * 输出 info 信息,用于提示用户
     * 
     */
    void i(String message);

    /**
     * 输出 debug 信息，用于调试
     */
    void d(String message);
}
