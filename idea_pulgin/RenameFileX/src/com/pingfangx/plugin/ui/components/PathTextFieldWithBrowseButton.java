package com.pingfangx.plugin.ui.components;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;

/**
 * 自定义以支持拖放
 *
 * @author pingfangx
 * @date 2018/7/1
 */
public class PathTextFieldWithBrowseButton extends ComponentWithBrowseButton<PathTextFieldWithBrowseButton.PathTextField> {

    public PathTextFieldWithBrowseButton(Project project, DropTargetListener dropTargetListener) {
        super(new PathTextField(project, new TextFieldWithAutoCompletion.StringsCompletionProvider(null, null), false, null, dropTargetListener), null);
    }

    /**
     * 在创建时设置拖放，获取的 PathTextField 是外部的范围，设置拖放只有内部的 EditorEx 才有用
     */
    public static class PathTextField extends TextFieldWithAutoCompletion<String> {
        private DropTargetListener mDropTargetListener;

        public PathTextField(@Nullable Project project, @NotNull TextFieldWithAutoCompletionListProvider<String> provider, boolean showCompletionHint, @Nullable String text, DropTargetListener dropTargetListener) {
            super(project, provider, showCompletionHint, text);
            mDropTargetListener = dropTargetListener;
        }

        @Override
        protected EditorEx createEditor() {
            //在创建时直接设置拖放，这里无法持有，要在调用 addNotify() 才初始化
            EditorEx mEditorEx = super.createEditor();
            if (mDropTargetListener != null) {
                new DropTarget(mEditorEx.getContentComponent(), DnDConstants.ACTION_COPY_OR_MOVE, mDropTargetListener);
            }
            return mEditorEx;
        }
    }
}
