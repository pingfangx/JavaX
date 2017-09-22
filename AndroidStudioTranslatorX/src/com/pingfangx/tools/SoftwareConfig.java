package com.pingfangx.tools;

import com.pingfangx.tools.base.IniConfig;

/**
 * 软件配置
 * 
 * @author 平方X
 *
 */
public class SoftwareConfig extends IniConfig {
    /**
     * as 目录
     */
    public static final String ANDROID_STUDIO_PATH = "android_studio_path";
    /**
     * 备份目录
     */
    public static final String BACKUP_PATH = "backup_path";
    /**
     * 汉化文件目录
     */
    public static final String TRANSLATION_PATH = "translation_path";
    /**
     * 备份路径
     */
    public static final String BACKUP_PATH_INDEX = "backup_path_index";
    /**
     * 备份文件类型
     */
    public static final String BACKUP_TYPE_INDEX = "backup_type_index";
    /**
     * 汉化类型
     */
    public static final String TRANSLATION_TYPE_INDEX = "translation_type_index";

    private static final String CONFIG_FILE_NAME = "AndroidStudioTranslatorX.ini";

    public SoftwareConfig() {
        super(CONFIG_FILE_NAME);
    }
}
