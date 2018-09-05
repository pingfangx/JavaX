package com.pingfangx.plugin.generatex.util

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.pingfangx.plugin.generatex.constant.Constant
import com.pingfangx.plugin.generatex.entity.Element
import com.pingfangx.plugin.generatex.entity.GenerateParams
import getTextFromStringsXml
import showPopupBalloon

/**
 *
 * @author pingfangx
 * @date 2018/9/5
 */
class FindViewByIdCreator(params: GenerateParams) : WriteCommandAction.Simple<Any>(params.project) {
    private val mProject: Project = params.project
    private val mEditor: Editor = params.editor
    private val mFile: PsiFile = params.file
    private val mClass: PsiClass = params.clazz
    private val mElements: ArrayList<Element> = params.elements
    private val mFactory: PsiElementFactory = params.factory
    private val mInitMethodName: String = params.initMethodName
    override fun run() {
        try {
            generateFindViewById()
        } catch (e: Exception) {
            e.printStackTrace()
            mEditor.showPopupBalloon(e.message, 10)
            return
        }

        //重写 class
        val styleManager = JavaCodeStyleManager.getInstance(mProject)
        styleManager.optimizeImports(mFile)
        styleManager.shortenClassReferences(mClass)
        val reformatCodeProcessor = ReformatCodeProcessor(mProject, mClass.containingFile, null, false)
        reformatCodeProcessor.runWithoutProgress()
        mEditor.showPopupBalloon(Constant.Action.SELECTED_SUCCESS)
    }

    /**
     * 任意类生成
     */
    private fun generateFindViewById() {
        //字段
        generateViewFields()
        //initView 方法
        generateInitViewMethod(mInitMethodName, "view")
    }

    /**
     * 创建字段
     */
    private fun generateViewFields() {
        outer@ for (element in mElements) {
            val fields = mClass.fields
            for (field in fields) {
                if (field.name == element.fieldName) {
                    // 已存在的变量就不创建
                    continue@outer
                }
            }
            // 设置变量名，获取text里面的内容
            if (element.isEnable) {
                // 添加字段到class
                val comment = getFieldComment(mProject, element, mFile)
                val fieldString = generateFieldString(element, comment)
                val psiField = mFactory.createFieldFromText(fieldString, mClass)
                mClass.add(psiField)
            }
        }
    }

    /**
     * 写initView方法
     */
    private fun generateInitViewMethod(methodName: String, paramName: String) {
        // 判断是否已有initView方法
        val initViewMethods = mClass.findMethodsByName(methodName, false)
        val initViewMethod = if (initViewMethods.isNotEmpty() && initViewMethods[0].body != null) initViewMethods[0] else null
        // 有initView方法
        if (initViewMethod != null) {
            val initViewMethodBody = initViewMethod.body
            // 获取initView方法里面的每条内容
            val statements = initViewMethodBody!!.statements
            mElements.filter { it.isEnable }
                    .forEach {
                        // 判断是否已存在findViewById
                        var fieldExists = false
                        val findViewById = "${it.fieldName} = $paramName.findViewById(${it.fullID});"
                        for (statement in statements) {
                            if (statement.text == findViewById) {
                                fieldExists = true
                                break
                            }
                        }
                        if (!fieldExists) {
                            // 不存在就添加字段
                            initViewMethodBody.add(mFactory.createStatementFromText(findViewById, initViewMethod))
                        }
                    }
        } else {
            //创建 initView 方法
            val initViewMethodString = generateInitViewMethodString(methodName, paramName, mElements)
            val newInitViewMethod = mFactory.createMethodFromText(initViewMethodString, mClass)
            mClass.add(newInitViewMethod)
        }
    }

    /**
     * 获取注释
     */
    private fun getFieldComment(project: Project, element: Element, psiFile: PsiFile): String? {
        // 如果是text为空，则获取hint里面的内容
        var text: String? = element.xml.getAttributeValue("android:text")
                ?: element.xml.getAttributeValue("android:hint")
        text?.let {
            if (it.contains("@string/")) {
                text = it.replace("@string/".toRegex(), "")
                // 获取strings.xml
                val psiFiles = FilenameIndex.getFilesByName(project, "strings.xml", GlobalSearchScope.allScope(project))
                if (psiFiles.isNotEmpty()) {
                    psiFiles
                            .filter {
                                // 获取src\main\res\values下面的strings.xml文件
                                it.parent != null && it.parent!!.toString().contains("src\\main\\res\\values", false)
                            }
                            .filter {
                                val psiFilePath = psiFile.parent?.toString()!!
                                val modulePath = it.parent?.toString()!!
                                psiFilePath.substring(0, psiFilePath.indexOf("\\main\\")) == modulePath.substring(0, modulePath.indexOf("\\main\\"))
                            }
                            .forEach { text = it.getTextFromStringsXml(text!!) }
                }
            }
        }
        return text
    }

    /**
     * 生成字段字符串
     */
    private fun generateFieldString(element: Element, comment: String?): String {
        val stringBuilder = StringBuilder()
        comment?.let {
            stringBuilder.append("/** $comment */\n")
        }
        with(element) {
            stringBuilder.append("private $name $fieldName")
        }
        stringBuilder.append(";")
        return stringBuilder.toString()
    }

    /**
     * 生成 initView 方法的字符串
     */
    private fun generateInitViewMethodString(methodName: String, paramName: String, elements: ArrayList<Element>): String {
        val initView = StringBuilder()
        initView.append("private void $methodName(View $paramName) {\n")
        elements.filter { it.isEnable }
                .forEach {
                    with(it) {
                        initView.append("$fieldName = ")
                        initView.append("$paramName.findViewById($fullID);\n")
                        if (isClickable && isClickEnable) {
                            initView.append("$fieldName.setOnClickListener(this);\n")
                        }
                    }
                }
        initView.append("}\n")
        return initView.toString()
    }
}