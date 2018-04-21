package com.pingfangx.plugin;

import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author pingfangx
 * @date 2018/4/21
 */
public class FileList extends JBList<String> {
    public FileList() {
        super(new DefaultListModel<>());
        init();
    }

    private void init() {
        //设置拖放
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    try {
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData instanceof List) {
                            for (Object next : ((List) transferData)) {
                                if (next instanceof File) {
                                    add(((File) next).getAbsolutePath());
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
    }

    @Override
    public DefaultListModel<String> getModel() {
        return (DefaultListModel<String>) super.getModel();
    }

    public void add(String file) {
        if (!getModel().contains(file)) {
            getModel().addElement(file);
        }
    }

    public void remove(Object file) {
        getModel().removeElement(file);
    }

    public void clear() {
        getModel().clear();
    }

    /**
     * 原本是处理上移、下移的
     * 可以用 listModel.add(index + 1, listModel.remove(index));
     * 但还是赋值性能比较好吧
     */
    public void exchange(int i, int j) {
        DefaultListModel<String> model = getModel();
        String t = model.get(i);
        model.set(i, model.get(j));
        model.set(j, t);
    }

    public void moveSelectionUp() {
        int[] selectedIndices = getSelectedIndices();
        //排序
        Arrays.sort(selectedIndices);
        if (selectedIndices.length == 0) {
            //为空
            return;
        }
        if (selectedIndices[0] == 0) {
            //第一个，无法上移
            return;
        }
        for (int i = 0; i < selectedIndices.length; i++) {
            //因为是与 index + 1 进行交换，这里先 -1
            exchange(selectedIndices[i] - 1, selectedIndices[i]);
            selectedIndices[i]--;
        }
        setSelectedIndices(selectedIndices);
    }

    public void moveSelectionDown() {

        int[] selectedIndices = getSelectedIndices();
        //排序
        Arrays.sort(selectedIndices);
        if (selectedIndices.length == 0) {
            //为空
            return;
        }
        if (selectedIndices[selectedIndices.length - 1] == getModel().getSize() - 1) {
            //最后一个元素是最后一个，无法下移
            return;
        }
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            //下移要倒序
            exchange(selectedIndices[i], selectedIndices[i] + 1);
            selectedIndices[i]++;
        }
        setSelectedIndices(selectedIndices);
    }

    public List<String> getFileList() {
        List<String> result = new ArrayList<>();
        DefaultListModel<String> model = getModel();
        for (int i = 0; i < model.getSize(); i++) {
            result.add(model.get(i));
        }
        return result;
    }
}
