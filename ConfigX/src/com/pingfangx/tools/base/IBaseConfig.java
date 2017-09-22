package com.pingfangx.tools.base;

/**
 * 软件配置
 * 
 * @author 平方X
 *
 */
public interface IBaseConfig {
    /**
     * 获取置值
     * 
     * @param name 值名
     */
    String get(String name);

    /**
     * 设置值
     * 
     * @param name 值名
     * @param value 值
     */
    void set(String name, String value);
}
