package com.pingfangx.plugin.generatex.entity

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFile
import java.util.*

/**
 *传递的参数
 * @author pingfangx
 * @date 2018/9/5
 */
data class GenerateParams(
        //相关文件
        val project: Project,
        val editor: Editor,
        val file: PsiFile,
        val clazz: PsiClass,
        /**
         * 各个元素
         */
        val elements: ArrayList<Element>,
        val factory: PsiElementFactory,
        /**
         * 初始化方法的方法名
         */
        val initMethodName: String
)