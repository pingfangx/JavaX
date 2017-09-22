package com.pingfangx.tools.base;

import java.io.File;
import java.io.IOException;

import org.dtools.ini.BasicIniFile;
import org.dtools.ini.IniFile;
import org.dtools.ini.IniFileReader;
import org.dtools.ini.IniFileWriter;
import org.dtools.ini.IniSection;

/**
 * ini 配置
 * 
 * @author 平方X
 *
 */
public class IniConfig implements IBaseConfig {
    private String mFilePath;
    private IniFile mIniFile;
    private IniFileReader mIniFileReader;
    private IniFileWriter mIniFileWriter;

    public IniConfig(String filePath) {
        mFilePath = filePath;
        mIniFile = new BasicIniFile();
    }

    private void initFileReader() {
        if (mIniFileReader == null) {
            File file = new File(mFilePath);
            if (file.exists()) {
                IniFileReader iniFileReader = new IniFileReader(mIniFile, file);
                try {
                    iniFileReader.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String get(String name) {
        initFileReader();
        if (mIniFile.getNumberOfSections() == 0) {
            return null;
        }
        return get(mIniFile.getSection(0), name);
    }

    public String get(IniSection section, String name) {
        initFileReader();
        if (section.hasItem(name)) {
            return section.getItem(name).getValue();
        }
        return null;
    }

    public void set(String name, String value) {
        if (mIniFile.getNumberOfSections() == 0) {
            mIniFile.addSection("settings");
        }
        set(mIniFile.getSection(0), name, value);
    }

    public void set(IniSection section, String name, String value) {
        if (section.hasItem(name)) {
            section.getItem(name).setValue(value);
        } else {
            section.addItem(name).setValue(value);
        }
        if (mIniFileWriter == null) {
            mIniFileWriter = new IniFileWriter(mIniFile, new File(mFilePath));
        }
        try {
            mIniFileWriter.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
