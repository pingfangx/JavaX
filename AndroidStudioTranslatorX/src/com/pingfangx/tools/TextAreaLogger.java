package com.pingfangx.tools;

import com.pingfangx.tools.base.SystemLogger;

import javafx.scene.control.TextArea;

/**
 * 用 TextArea 输出
 *
 */
public class TextAreaLogger extends SystemLogger {
    private TextArea mTextArea;

    public TextAreaLogger(TextArea textArea) {
        mTextArea = textArea;
    }

    @Override
    public void i(String message) {
        super.i(message);
        mTextArea.appendText("\n" + message);
        mTextArea.setScrollTop(Double.MAX_VALUE);
    }

}
