package com.pingfangx.demo.plugin.shake;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author pingfangx
 * @date 2018/4/19
 */
public class ShakePluginComponent implements ApplicationComponent {
    private Project mProject;
    private Timer mTimer;

    @Override
    public void initComponent() {
        LogUtils.d("initComponent");
        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectOpened(Project project) {
                onProjectOpened(project);
            }
        });
    }


    @Override
    public void disposeComponent() {
        LogUtils.d("disposeComponent");
    }

    private void onProjectOpened(Project project) {
        LogUtils.d("onProjectOpened");
        mProject = project;
        mTimer = new Timer();
        //使用 message bus
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                onFileOpened(source, file);
            }
        });
    }

    private void onFileOpened(FileEditorManager source, VirtualFile file) {
        LogUtils.d("onFileOpened");
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(DocumentEvent event) {
                    shake();
                }
            });
        }
    }

    private void shake() {
        Editor selectedTextEditor = FileEditorManager.getInstance(mProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            JComponent contentComponent = selectedTextEditor.getContentComponent();
            if (mTimer != null) {
                //需要使用 timer
                mTimer.schedule(new TimerTask() {
                    public void run() {
                        int x = 10;
                        int y = 10;
                        LogUtils.d("do shake");
                        contentComponent.setLocation(x, y);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        contentComponent.setLocation(0, 0);
                    }
                }, 1);
            }
        }
    }

}
