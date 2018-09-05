package com.pingfangx.plugin.generatex.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtilBase
import com.intellij.psi.xml.XmlFile
import com.pingfangx.plugin.generatex.constant.Constant
import com.pingfangx.plugin.generatex.entity.Element
import com.pingfangx.plugin.generatex.view.GenerateDialog
import getIDsFromLayoutToList
import getTargetClass
import showPopupBalloon
import java.util.*

/**
 * 基于 <a href="https://github.com/wangzailfm/GenerateFindViewById">GenerateFindViewById</a> ，感谢。
 * @author pingfangx
 * @date 2018/9/5
 */
class FindViewByIdAction : AnAction() {
    private var mDialog: GenerateDialog? = null
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return
        //取选中，选中为空则输入，输入为空则报错
        val selectedText: String = editor.selectionModel.selectedText ?: let {
            Messages.showInputDialog(project,
                    Constant.Action.SELECTED_MESSAGE,
                    Constant.Action.SELECTED_TITLE,
                    Messages.getInformationIcon()) ?: let {
                editor.showPopupBalloon(Constant.Action.SELECTED_ERROR_NO_NAME)
                return
            }
        }
        //取文件与类
        val currentFile = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        val currentClass = editor.getTargetClass(currentFile) ?: let {
            editor.showPopupBalloon(Constant.Action.SELECTED_ERROR_NO_POINT)
            return
        }
        // 获取布局文件，通过FilenameIndex.getFilesByName获取GlobalSearchScope.allScope(project)搜索整个项目
        var xmlFiles = FilenameIndex.getFilesByName(project,
                selectedText + Constant.Action.SELECTED_TEXT_SUFFIX,
                GlobalSearchScope.allScope(project))
        if (xmlFiles.isEmpty()) {
            //添加前缀重新搜索
            if (selectedText.startsWith(Constant.Action.SELECTED_TEXT_ACTIVITY_PREFIX).not()) {
                xmlFiles = FilenameIndex.getFilesByName(project,
                        Constant.Action.SELECTED_TEXT_ACTIVITY_PREFIX + selectedText + Constant.Action.SELECTED_TEXT_SUFFIX,
                        GlobalSearchScope.allScope(project))
            }
        }
        if (xmlFiles.isEmpty()) {
            editor.showPopupBalloon(Constant.Action.SELECTED_ERROR_NO_SELECTED)
            return
        }
        //取 xml 文件
        val xmlFile = if (xmlFiles.size > 1) {
            //多个文件，选择同目录的
            val currentFileDir = currentFile.parent?.toString()!!
            val srcDir = getSrcDir(currentFileDir)
            val sameSrcDirXmlFiles = xmlFiles.filter {
                val xmlFileDir = it.parent?.toString()!!
                xmlFileDir.contains("\\src\\main\\res\\layout") && srcDir == getSrcDir(xmlFileDir)
            }
            if (sameSrcDirXmlFiles.isEmpty()) {
                editor.showPopupBalloon(Constant.Action.SELECTED_ERROR_NO_SELECTED)
                return
            } else {
                sameSrcDirXmlFiles[0] as XmlFile
            }
        } else {
            xmlFiles[0] as XmlFile
        }


        //取元素
        val elements = ArrayList<Element>()
        xmlFile.getIDsFromLayoutToList(elements)
        if (elements.size == 0) {
            editor.showPopupBalloon(Constant.Action.SELECTED_ERROR_NO_ID)
            return
        }
        //显示对话框
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog?.cancelDialog()
        }
        mDialog = GenerateDialog(
                mEditor = editor,
                elements = elements,
                mPsiFile = currentFile,
                psiClass = currentClass,
                elementSize = elements.size
        )
        mDialog?.showDialog()
    }

    private fun getSrcDir(path: String) = path.substring(0, path.indexOf("\\main\\"))
}