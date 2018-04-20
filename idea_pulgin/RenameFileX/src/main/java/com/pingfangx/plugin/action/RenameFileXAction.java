package com.pingfangx.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.pingfangx.plugin.RenameFileXDialog;

/**
 * @author pingfangx
 * @date 2018/4/20
 */
public class RenameFileXAction extends AnAction {
    public RenameFileXAction() {
        super("复制并重命名文件", "选择一个或多个文件，复制到项目中并重命名为指定文件", null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        RenameFileXDialog dialog = new RenameFileXDialog(project);
        dialog.show();
    }
}
