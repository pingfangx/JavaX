package com.pingfangx.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.pingfangx.plugin.ui.components.FileList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对话框中使用的相关操作
 *
 * @author pingfangx
 * @date 2018/7/1
 */
public class FileListActions {

    /**
     * 文件列表的操作
     */
    static abstract class FileListAction extends AnAction {
        protected FileList mFileList;
        protected FileList mAnotherFileList;

        public FileListAction(Icon icon, @NotNull FileList fileList) {
            this(null, icon, fileList);
        }

        public FileListAction(String text, Icon icon, FileList fileList) {
            this(text, null, icon, fileList);
        }

        public FileListAction(String text, String desc, Icon icon, FileList fileList) {
            this(text, desc, icon, fileList, null);
        }

        public FileListAction(String text, String desc, Icon icon, FileList fileList, FileList anotherFileList) {
            super(text, desc == null || desc.length() == 0 ? text : desc, icon);
            mFileList = fileList;
            mAnotherFileList = anotherFileList;
        }
    }

    /**
     * 添加
     */
    static class AddAction extends FileListAction {
        public AddAction(FileList mList) {
            super("添加文件", AllIcons.General.Add, mList);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, true);
            VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, e.getProject(), null);
            for (VirtualFile file : virtualFiles) {
                mFileList.add(file.getPath());
            }
        }
    }

    /**
     * 移除
     */
    static class RemoveAction extends FileListAction {
        public RemoveAction(FileList mList) {
            super("移除文件", AllIcons.General.Remove, mList);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            //不需判 null
            for (String s : mFileList.getSelectedValuesList()) {
                mFileList.remove(s);
            }
        }
    }

    /**
     * 清除
     */
    static class ClearAction extends FileListAction {
        public ClearAction(@NotNull FileList list) {
            super("清空文件", AllIcons.Actions.GC, list);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.clear();
        }
    }

    /**
     * 将上方输入框的内容应用到下方文件列表
     */
    static class ApplyPathAction extends FileListAction {
        private EditorTextField mPathTextField;

        public ApplyPathAction(@NotNull FileList list, EditorTextField textField, boolean left) {
            super("从输入框读取文件", left ? AllIcons.Diff.ArrowLeftDown : AllIcons.Diff.ArrowRightDown, list);
            this.mPathTextField = textField;
            //3个都注册
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(left ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK)), mPathTextField);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(left ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK)), mFileList);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(left ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK)), mAnotherFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            String path = mPathTextField.getText();
            if (path.length() != 0) {
                mFileList.add(path);
            }
        }
    }

    /**
     * 上移
     */
    static class MoveUpAction extends FileListAction {
        public MoveUpAction(@NotNull FileList list) {
            super("向上移动", AllIcons.Actions.MoveUp, list);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.moveSelectionUp();
        }
    }

    /**
     * 下移
     */
    static class MoveDownAction extends FileListAction {
        public MoveDownAction(@NotNull FileList list) {
            super("向下移动", AllIcons.Actions.MoveDown, list);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.moveSelectionDown();
        }
    }

    /**
     * 向另一方移动
     */
    static class MoveToAnotherAction extends FileListAction {
        public MoveToAnotherAction(FileList fileList, FileList anotherFileList, boolean left) {
            super(left ? "向右移动" : "向左移动", "", left ? AllIcons.Diff.ArrowRight : AllIcons.Diff.Arrow, fileList, anotherFileList);
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(left ? KeyEvent.VK_RIGHT : KeyEvent.VK_LEFT, 0)), mFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            for (String s : mFileList.getSelectedValuesList()) {
                mFileList.remove(s);
                mAnotherFileList.add(s);
            }
        }
    }

    /**
     * 左右交换
     */
    static class ExchangeAnotherAction extends FileListAction {
        public ExchangeAnotherAction(FileList fileList, FileList anotherFileList) {
            super("左右交换", "", AllIcons.Diff.ApplyNotConflicts, fileList, anotherFileList);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            int[] selectedIndices1 = mFileList.getSelectedIndices();
            int[] selectedIndices2 = mAnotherFileList.getSelectedIndices();
            if (selectedIndices1.length == selectedIndices2.length) {
                //先排序，保证后面倒序删除时不错乱
                Arrays.sort(selectedIndices1);
                Arrays.sort(selectedIndices2);

                //保存选中的文件
                List<String> selectedValuesList1 = new ArrayList<>();
                for (int i : selectedIndices1) {
                    selectedValuesList1.add(mFileList.getModel().get(i));
                }
                List<String> selectedValuesList2 = new ArrayList<>();
                for (int i : selectedIndices2) {
                    selectedValuesList2.add(mAnotherFileList.getModel().get(i));
                }

                //倒序移除，保证添加时不会因为重复而造成位置顺序不正确
                for (int i = selectedIndices1.length - 1; i >= 0; i--) {
                    mFileList.getModel().remove(selectedIndices1[i]);
                }
                for (int i = selectedIndices2.length - 1; i >= 0; i--) {
                    mAnotherFileList.getModel().remove(selectedIndices2[i]);
                }

                //按原来的选中的位置任入保存的文件
                for (int i = 0; i < selectedValuesList1.size(); i++) {
                    // 1 的加到 2
                    String s = selectedValuesList1.get(i);
                    int index = selectedIndices2[i];
                    mAnotherFileList.getModel().add(index, s);
                }
                for (int i = 0; i < selectedValuesList2.size(); i++) {
                    // 2 的加到 1
                    String s = selectedValuesList2.get(i);
                    mFileList.getModel().add(selectedIndices1[i], s);
                }
                //选中原来的选面
                mFileList.setSelectedIndices(selectedIndices1);
                mAnotherFileList.setSelectedIndices(selectedIndices2);
            }
        }
    }

}
