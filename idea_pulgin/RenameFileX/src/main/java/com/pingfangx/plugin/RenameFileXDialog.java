package com.pingfangx.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.structuralsearch.plugin.ui.TextFieldWithAutoCompletionWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBDimension;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author pingfangx
 * @date 2018/4/20
 */
public class RenameFileXDialog extends DialogWrapper {
    private Project mProject;
    private TextFieldWithAutoCompletionWithBrowseButton mPathTextField;
    private FileList mLeftList;
    private FileList mRightList;
    private JBLabel mErrorLabel;
    private JProgressBar mActionProgressBar;
    private JLabel mActionProgressLabel;

    public RenameFileXDialog(@Nullable Project project) {
        super(project, true);
        mProject = project;
        init();
    }

    @Override
    protected void init() {
        super.init();
        setTitle("复制并替换文件");
        resetUI();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel wholePanel = new JPanel(new VerticalLayout());

        //输入地址
        mPathTextField = new TextFieldWithAutoCompletionWithBrowseButton(mProject);
        mPathTextField.getChildComponent().setPlaceholder("选择或输入文件名，点箭头添加到下方的文件列表中");
        mPathTextField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
                FileChooser.chooseFile(descriptor, mProject, null, virtualFile -> mPathTextField.setText(virtualFile.getCanonicalPath()));
            }
        });
        wholePanel.add(mPathTextField);

        //左右文件列表
        Splitter listSplitter = new Splitter();
        listSplitter.setFirstComponent(createPanel(true));
        listSplitter.setSecondComponent(createPanel(false));

        wholePanel.add(listSplitter);

        //错误信息
        mErrorLabel = new JBLabel();
        wholePanel.add(mErrorLabel);

        //进度
        mActionProgressLabel = new JBLabel();
        wholePanel.add(mActionProgressLabel);

        mActionProgressBar = new JProgressBar();
        wholePanel.add(mActionProgressBar);


        return wholePanel;
    }


    private JComponent createPanel(boolean left) {
        JPanel panel = new JPanel(new VerticalLayout());
        //添加标题
        TitledSeparator titledSeparator = new TitledSeparator(left ? "源文件" : "目标文件");
        panel.add(titledSeparator);


        FileList list = new FileList();
        if (left) {
            mLeftList = list;
        } else {
            mRightList = list;
        }

        //添加工具栏
        panel.add(createToolbar(list, left));

        //添加列表
        list.setEmptyText(left ? "点击 + 添加要复制进项目的源文件" : "点击 + 添加要覆盖的目标文件");
        list.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                resetUI();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                resetUI();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                resetUI();
            }
        });
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(list);
        scrollPane.setPreferredSize(new JBDimension(500, 200));
        panel.add(scrollPane);

        return panel;
    }

    private void resetUI() {
        mErrorLabel.setForeground(JBColor.BLACK);
        mErrorLabel.setText("信息");
        updateCopyProgress(0, 0);
    }


    private JComponent createToolbar(FileList list, boolean left) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddAction(list));
        group.add(new RemoveAction(list));
        group.add(new ClearAction(list));
        group.add(new ApplyPathAction(list, left));
        group.add(new MoveUpAction(list));
        group.add(new MoveDownAction(list));
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("PackageDependencies", group, true);
        return toolbar.getComponent();
    }

    @Override
    protected void doOKAction() {
        ListModel leftModel = mLeftList.getModel();
        ListModel rightModel = mRightList.getModel();
        if (leftModel != null && rightModel != null) {
            if (leftModel.getSize() == 0) {
                showError("没有选择源文件");
                return;
            }
            if (rightModel.getSize() == 0) {
                showError("没有选择目标文件");
                return;
            }
            if (leftModel.getSize() != rightModel.getSize()) {
                showError("源文件与目标文件数量不相等");
                return;
            }
            copyFiles(mLeftList.getFileList(), mRightList.getFileList());
            return;
        }
        super.doOKAction();
    }

    private void copyFiles(List<String> source, List<String> destination) {
        if (source == null || source.isEmpty() || destination == null || destination.isEmpty()) {
            return;
        }
        int size;
        if ((size = source.size()) != destination.size()) {
            return;
        }
        for (int i = 0; i < size; i++) {
            copyFile(source.get(i), destination.get(i));
            updateCopyProgress(i + 1, size);
        }
    }

    private static void copyFile(String source, String destination) {
        try {
            FileUtil.copy(new File(source), new File(destination));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCopyProgress(int index, int all) {
        mActionProgressBar.setMinimum(0);
        mActionProgressBar.setMaximum(all);
        mActionProgressBar.setValue(index);
        mActionProgressLabel.setText(getProgressText(index, all));
    }

    private String getProgressText(int index, int all) {
        return String.format("进度： %d/%d", index, all);
    }

    private void showError(String error) {
        mErrorLabel.setForeground(JBColor.RED);
        mErrorLabel.setText(error);
    }


    /**
     * 文件列表的操作
     */
    private static abstract class FileListAction extends AnAction {
        protected FileList mFileList;

        public FileListAction(@NotNull FileList fileList, Icon icon) {
            super(icon);
            mFileList = fileList;
        }
    }

    private static class AddAction extends FileListAction {
        public AddAction(FileList mList) {
            super(mList, AllIcons.General.Add);
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

    private static class RemoveAction extends FileListAction {
        public RemoveAction(FileList mList) {
            super(mList, AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            //不需判 null
            for (Object o : mFileList.getSelectedValuesList()) {
                mFileList.remove(o);
            }
        }
    }

    private static class ClearAction extends FileListAction {
        public ClearAction(@NotNull FileList list) {
            super(list, AllIcons.Actions.GC);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.clear();
        }
    }

    private class ApplyPathAction extends FileListAction {
        public ApplyPathAction(@NotNull FileList list, boolean left) {
            super(list, left ? AllIcons.Diff.ArrowLeftDown : AllIcons.Diff.ArrowRightDown);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            String path = mPathTextField.getText();
            if (path != null && path.length() != 0) {
                mFileList.add(path);
            }
        }
    }

    private class MoveUpAction extends FileListAction {
        public MoveUpAction(@NotNull FileList list) {
            super(list, AllIcons.Actions.MoveUp);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.moveSelectionUp();
        }
    }

    private class MoveDownAction extends FileListAction {
        public MoveDownAction(@NotNull FileList list) {
            super(list, AllIcons.Actions.MoveDown);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            mFileList.moveSelectionDown();
        }
    }


}
