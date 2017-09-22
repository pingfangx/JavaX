package com.pingfangx.tools.base;

/**
 * 用系统输出
 * 
 * @author 平方X
 *
 */
public class SystemLogger implements ILogger {

    @Override
    public void i(String message) {
        System.out.println(message);
    }

    @Override
    public void d(String message) {
        System.out.println(message);
    }

}
