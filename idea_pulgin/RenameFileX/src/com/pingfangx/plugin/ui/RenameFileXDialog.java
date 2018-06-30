package com.pingfangx.plugin.ui;

import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBDimension;
import com.pingfangx.plugin.ui.components.FileList;
import com.pingfangx.plugin.ui.components.PathTextFieldWithBrowseButton;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author pingfangx
 * @date 2018/4/20
 */
public class RenameFileXDialog extends DialogWrapper {
    private Project mProject;
    private EditorTextField mPathTextField;
    private FileList mLeftFileList;
    private FileList mRightFileList;
    //用来显示进度
    private JBLabel mErrorLabel;
    private JProgressBar mActionProgressBar;
    private JLabel mActionProgressLabel;

    public RenameFileXDialog(@Nullable Project project, VirtualFile[] files) {
        super(project, true, true);
        mProject = project;
        init();
        initSelectedFile(files);
    }

    @Override
    protected void init() {
        super.init();
        //取消模态，方便拖入文件
        setModal(false);
        setTitle("复制并替换文件");
        resetUI();
    }

    /**
     * 初始化选中的文件
     * 将选中的文件添加到右边的目标框
     */
    private void initSelectedFile(VirtualFile[] files) {
        if (files != null) {
            VirtualFile uniqueDir;
            if (files.length == 1 && (uniqueDir = files[0]).isDirectory()) {
                //如果只有一个目录，将其置为上方输入框内容
                mPathTextField.setText(uniqueDir.getPath());
            } else {
                for (VirtualFile file : files) {
                    if (!file.isDirectory()) {
                        mRightFileList.add(file.getPath());
                    }
                }
            }
        }
    }

    /**
     * 重置
     */
    private void resetUI() {
        mErrorLabel.setForeground(JBColor.BLACK);
        mErrorLabel.setText("信息");
        updateCopyProgress(0, 0);
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel wholePanel = new JPanel(new VerticalLayout());
        wholePanel.add(new JLabel("输入源文件名或目标文件"));

        //输入地址
        PathTextFieldWithBrowseButton pathTextFieldWithBrowseButton = new PathTextFieldWithBrowseButton(mProject, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    try {
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData instanceof List) {
                            if (((List) transferData).size() == 1) {
                                Object first = ((List) transferData).get(0);
                                if (first instanceof File) {
                                    File file = (File) first;
                                    //目录也可以拖入
                                    mPathTextField.setText(file.getAbsolutePath());
                                }
                            }
                        }
                    } catch (UnsupportedFlavorException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        });
        mPathTextField = pathTextFieldWithBrowseButton.getChildComponent();
        mPathTextField.setPlaceholder("选择或输入文件名，点箭头添加到下方的文件列表中");
        pathTextFieldWithBrowseButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
                FileChooser.chooseFile(descriptor, mProject, null, virtualFile -> mPathTextField.setText(virtualFile.getCanonicalPath()));
            }
        });

        wholePanel.add(pathTextFieldWithBrowseButton);

        //左右文件列表
        Splitter listSplitter = new Splitter();
        mLeftFileList = new FileList();
        mRightFileList = new FileList();
        listSplitter.setFirstComponent(createPanel(true, mLeftFileList, mRightFileList));
        listSplitter.setSecondComponent(createPanel(false, mRightFileList, mLeftFileList));

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


    /**
     * 创建左右的文件列表
     */
    private JComponent createPanel(boolean left, FileList list, FileList anotherFileList) {
        JPanel panel = new JPanel(new VerticalLayout());
        //添加标题
        TitledSeparator titledSeparator = new TitledSeparator(left ? "源文件" : "目标文件");
        panel.add(titledSeparator);
        //添加工具栏
        panel.add(createToolbar(left, list, anotherFileList));

        //添加列表
        list.setEmptyText(left ? "点击 + 添加或拖入要复制进项目的源文件" : "点击 + 添加或拖入要覆盖的目标文件");
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
        list.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedIndex() != -1) {
                    if (e.getClickCount() == 2) {
                        //双击，添加上去
                        mPathTextField.setText(list.getSelectedValue());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(list);
        scrollPane.setPreferredSize(new JBDimension(500, 200));
        panel.add(scrollPane);

        return panel;
    }

    /**
     * 创建工具栏
     */
    private JComponent createToolbar(boolean left, FileList list, FileList anotherFileList) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new FileListActions.AddAction(list));
        group.add(new FileListActions.RemoveAction(list));
        group.add(new FileListActions.ClearAction(list));
        group.add(new FileListActions.ApplyPathAction(list, mPathTextField, left));
        group.add(new FileListActions.MoveUpAction(list));
        group.add(new FileListActions.MoveDownAction(list));
        group.add(new FileListActions.MoveToAnotherAction(list, anotherFileList, left));
        group.add(new FileListActions.ExchangeAnotherAction(list, anotherFileList));
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("PackageDependencies", group, true);
        return toolbar.getComponent();
    }

    @Override
    protected void doOKAction() {
        ListModel leftModel = mLeftFileList.getModel();
        ListModel rightModel = mRightFileList.getModel();
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
            copyFiles(mLeftFileList.getFileList(), mRightFileList.getFileList());
            return;
        }
        super.doOKAction();
    }

    /**
     * 复制文件
     */
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

    private void copyFile(String source, String destination) {
        try {
            FileUtil.copy(new File(source), new File(destination));
        } catch (IOException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    private void updateCopyProgress(int index, int all) {
        mActionProgressBar.setMinimum(0);
        mActionProgressBar.setMaximum(all);
        mActionProgressBar.setValue(index);
        if (all > 0 && index == all) {
            mActionProgressLabel.setText(getProgressText(index, all) + " 操作完成");
            onCopySuccess();
        } else {
            mActionProgressLabel.setText(getProgressText(index, all));
        }
    }

    /**
     * 复制完成
     */
    private void onCopySuccess() {
        //com.intellij.ide.actions.SynchronizeAction
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
    }

    private String getProgressText(int index, int all) {
        return String.format("进度： %d/%d", index, all);
    }

    private void showError(String error) {
        mErrorLabel.setForeground(JBColor.RED);
        mErrorLabel.setText(error);
    }

}
