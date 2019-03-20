package com.pingfangx.plugin.generatex.view

import com.intellij.openapi.editor.Editor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBScrollPane
import com.pingfangx.plugin.generatex.constant.Constant
import com.pingfangx.plugin.generatex.entity.Element
import com.pingfangx.plugin.generatex.entity.GenerateParams
import com.pingfangx.plugin.generatex.entity.IdBean
import com.pingfangx.plugin.generatex.util.FindViewByIdCreator
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * GenerateDialog
 * @author Jowan
 */
open class GenerateDialog(
        private val mEditor: Editor,
        /*获取mElements*/
        private val elements: ArrayList<Element>,
        /*获取当前文件*/
        private val mPsiFile: PsiFile,
        /*获取class*/
        private val psiClass: PsiClass,
        /*判断是否全选*/
        private var elementSize: Int
) : JFrame(), ActionListener, ItemListener {


    // 判断OnClick是否全选
    private var mOnClickSize: Int = 0

    // 标签JPanel
    private val mPanelTitle = JPanel()
    private val mTitleName = JCheckBox(Constant.Dialog.TABLE_FIELD_VIEW_WIDGET)
    private val mTitleId = JLabel(Constant.Dialog.TABLE_FIELD_VIEW_ID)
    private val mTitleClick = JCheckBox(Constant.Dialog.FIELD_ON_CLICK, false)
    // 命名JPanel
    private val mPanelTitleField = JPanel()
    private val mTitleFieldGroup = ButtonGroup()
    // aa_bb
    private val mTitleFieldUnderline = JRadioButton("aa_bb")
    // aaBb
    private val mTitleFieldHump = JRadioButton("aaBb")
    // mAaBb
    private val mTitleFieldPrefix = JRadioButton("mAaBb", true)

    // 内容JPanel
    private val mContentJPanel = JPanel()
    private val mContentLayout = GridBagLayout()
    private val mContentConstraints = GridBagConstraints()
    // 内容JBScrollPane滚动
    private lateinit var jScrollPane: JBScrollPane

    // 底部JPanel
    // LayoutInflater JPanel
    private val mPanelInflater = JPanel(FlowLayout(FlowLayout.LEFT))
    /**
     * 初始化方法名
     */
    private lateinit var mTextFieldInitMethod: JTextField
    private var type = 3

    // viewHolder
    private val mPanelViewHolder = JPanel(FlowLayout(FlowLayout.LEFT))

    // 是否需要强转
    private val mPanelNeedCasts = JPanel(FlowLayout(FlowLayout.LEFT))

    // 确定、取消JPanel
    private val mPanelButtonRight = JPanel()
    private val mButtonConfirm = JButton(Constant.Dialog.BUTTON_CONFIRM)
    private val mButtonCancel = JButton(Constant.Dialog.BUTTON_CANCEL)

    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private val mLayout = GridBagLayout()
    // GridBagConstraints用来控制添加进的组件的显示位置
    private val mConstraints = GridBagConstraints()


    init {
        initTopPanel()
        initContentPanel()
        setCheckAll()
        initBottomPanel()
        setConstraints()
        setDialog()
    }

    /**
     * 全选设置
     */
    private fun setCheckAll() {
        elements
                .filter { it.isClickable }
                .forEach { mOnClickSize++ }
        mTitleName.isSelected = elementSize == elements.size
        mTitleName.addActionListener(this)
        mTitleClick.isSelected = mOnClickSize == elements.size
        mTitleClick.addActionListener(this)
    }

    /**
     * 添加头部
     */
    private fun initTopPanel() {
        mPanelTitle.layout = GridLayout(1, 4, 8, 10)
        mPanelTitle.border = EmptyBorder(5, 10, 5, 10)
        mPanelTitleField.layout = GridLayout(1, 3, 0, 0)
        mTitleName.horizontalAlignment = JLabel.LEFT
        mTitleName.border = EmptyBorder(0, 5, 0, 0)
        mTitleId.horizontalAlignment = JLabel.LEFT
        mTitleClick.horizontalAlignment = JLabel.LEFT
        // 添加listener
        mTitleFieldUnderline.addItemListener(this)
        mTitleFieldHump.addItemListener(this)
        mTitleFieldPrefix.addItemListener(this)
        // 添加到group
        mTitleFieldGroup.add(mTitleFieldUnderline)
        mTitleFieldGroup.add(mTitleFieldHump)
        mTitleFieldGroup.add(mTitleFieldPrefix)
        // 添加到JPanel
        mPanelTitleField.add(mTitleFieldUnderline)
        mPanelTitleField.add(mTitleFieldHump)
        mPanelTitleField.add(mTitleFieldPrefix)
        // 添加到JPanel
        mPanelTitle.add(mTitleName)
        mPanelTitle.add(mTitleId)
        mPanelTitle.add(mTitleClick)
        mPanelTitle.add(mPanelTitleField)
        mPanelTitle.setSize(900, 30)
        // 添加到JFrame
        contentPane.add(mPanelTitle, 0)
    }

    /**
     * 添加底部
     */
    private fun initBottomPanel() {
        // 添加监听
        mButtonConfirm.addActionListener(this)
        mButtonCancel.addActionListener(this)
        // viewHolder
//        mPanelViewHolder.add(mViewHolderCheck)
        // casts
//        mNeedCastsCheck.isEnabled = !mIsButterKnife
//        mPanelNeedCasts.add(mNeedCastsCheck)
        // 右边
        mPanelButtonRight.add(mButtonConfirm)
        mPanelButtonRight.add(mButtonCancel)
        // 添加到JPanel
        //mPanelInflater.add(mCheckAll);
//        mPanelInflater.add(mLayoutInflater)
//        mPanelInflater.add(mLayoutInflaterField)
        mPanelInflater.add(JLabel(Constant.Dialog.FIELD_INIT_METHOD))
        mTextFieldInitMethod = JTextField("initViews(view)", 20)
        mPanelInflater.add(mTextFieldInitMethod)
        // 添加到JFrame
        contentPane.add(mPanelInflater, 2)
        contentPane.add(mPanelViewHolder, 3)
        contentPane.add(mPanelNeedCasts, 4)
        contentPane.add(mPanelButtonRight, 5)
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private fun initContentPanel() {
        mContentJPanel.removeAll()
        // 设置内容
        for (i in elements.indices) {
            val element = elements[i]
            val itemJPanel = IdBean(GridLayout(1, 4, 10, 10),
                    EmptyBorder(5, 10, 5, 10),
                    JCheckBox(element.name),
                    JLabel(element.id),
                    JCheckBox(),
                    JTextField(element.fieldName),
                    element.isEnable,
                    element.isClickable,
                    element.isClickEnable)
            // 监听
            itemJPanel.setEnableActionListener { enableCheckBox ->
                element.isEnable = enableCheckBox.isSelected
                elementSize = if (enableCheckBox.isSelected) elementSize + 1 else elementSize - 1
                mTitleName.isSelected = elementSize == elements.size
            }
            itemJPanel.setClickActionListener { clickCheckBox ->
                element.isClickable = clickCheckBox.isSelected
                mOnClickSize = if (clickCheckBox.isSelected) mOnClickSize + 1 else mOnClickSize - 1
                mTitleClick.isSelected = mOnClickSize == elements.size
            }
            itemJPanel.setFieldFocusListener { fieldJTextField -> element.fieldName = fieldJTextField.text }
            mContentJPanel.add(itemJPanel)
            mContentConstraints.fill = GridBagConstraints.HORIZONTAL
            mContentConstraints.gridwidth = 0
            mContentConstraints.gridx = 0
            mContentConstraints.gridy = i
            mContentConstraints.weightx = 1.0
            mContentLayout.setConstraints(itemJPanel, mContentConstraints)
        }
        mContentJPanel.layout = mContentLayout
        jScrollPane = JBScrollPane(mContentJPanel)
        jScrollPane.revalidate()
        // 添加到JFrame
        contentPane.add(jScrollPane, 1)
    }

    /**
     * 设置Constraints
     */
    private fun setConstraints() {
        // 使组件完全填满其显示区域
        mConstraints.fill = GridBagConstraints.BOTH
        // 设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        mConstraints.gridwidth = 0
        // 第几列
        mConstraints.gridx = 0
        // 第几行
        mConstraints.gridy = 0
        // 行拉伸0不拉伸，1完全拉伸
        mConstraints.weightx = 1.0
        // 列拉伸0不拉伸，1完全拉伸
        mConstraints.weighty = 0.0
        // 设置组件
        mLayout.setConstraints(mPanelTitle, mConstraints)
        mConstraints.fill = GridBagConstraints.BOTH
        mConstraints.gridwidth = 1
        mConstraints.gridx = 0
        mConstraints.gridy = 1
        mConstraints.weightx = 1.0
        mConstraints.weighty = 1.0
        mLayout.setConstraints(jScrollPane, mConstraints)
        mConstraints.fill = GridBagConstraints.HORIZONTAL
        mConstraints.gridwidth = 0
        mConstraints.gridx = 0
        mConstraints.gridy = 2
        mConstraints.weightx = 1.0
        mConstraints.weighty = 0.0
        mLayout.setConstraints(mPanelInflater, mConstraints)
        mConstraints.fill = GridBagConstraints.HORIZONTAL
        mConstraints.gridwidth = 0
        mConstraints.gridx = 0
        mConstraints.gridy = 3
        mConstraints.weightx = 1.0
        mConstraints.weighty = 0.0
        mLayout.setConstraints(mPanelViewHolder, mConstraints)
        mConstraints.fill = GridBagConstraints.HORIZONTAL
        mConstraints.gridwidth = 0
        mConstraints.gridx = 0
        mConstraints.gridy = 4
        mConstraints.weightx = 1.0
        mConstraints.weighty = 0.0
        mLayout.setConstraints(mPanelNeedCasts, mConstraints)
        mConstraints.fill = GridBagConstraints.NONE
        mConstraints.gridwidth = 0
        mConstraints.gridx = 0
        mConstraints.gridy = 5
        mConstraints.weightx = 0.0
        mConstraints.weighty = 0.0
        mConstraints.anchor = GridBagConstraints.EAST
        mLayout.setConstraints(mPanelButtonRight, mConstraints)
    }

    /**
     * 显示dialog
     */
    fun showDialog() {
        // 显示
        isVisible = true
    }

    /**
     * 设置JFrame参数
     */
    private fun setDialog() {
        // 设置标题
        title = Constant.Dialog.TITLE
        // 设置布局管理
        layout = mLayout
        // 可拉伸
        isResizable = true
        // 设置大小
        setSize(1000, 800)
        // 自适应大小
        // pack();
        // 设置居中，放在setSize后面
        setLocationRelativeTo(null)
        // 显示最前
        isAlwaysOnTop = true
    }

    /**
     * 关闭dialog
     */
    fun cancelDialog() {
        isVisible = false
        dispose()
    }

    /**
     * 刷新JScrollPane内容
     */
    private fun refreshJScrollPane() {
        remove(jScrollPane)
        initContentPanel()
        setConstraints()
        revalidate()
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.actionCommand) {
            Constant.Dialog.BUTTON_CONFIRM -> {
                cancelDialog()
                val params = GenerateParams(psiClass.project, mEditor, mPsiFile, psiClass, elements, JavaPsiFacade.getElementFactory(psiClass.project), mTextFieldInitMethod.text)
                FindViewByIdCreator(params).execute()
            }
            Constant.Dialog.BUTTON_CANCEL -> cancelDialog()
            Constant.Dialog.FIELD_ON_CLICK -> {
                // 刷新
                for (element in elements) {
                    element.isClickable = mTitleClick.isSelected
                }
                mOnClickSize = if (mTitleClick.isSelected) elements.size else 0
                refreshJScrollPane()
            }
            Constant.Dialog.TABLE_FIELD_VIEW_WIDGET -> {
                // 刷新
                for (element in elements) {
                    element.isEnable = mTitleName.isSelected
                }
                elementSize = if (mTitleName.isSelected) elements.size else 0
                refreshJScrollPane()
            }
        }
    }

    override fun itemStateChanged(e: ItemEvent) {
        type = when (e.source) {
            mTitleFieldPrefix -> 3
            mTitleFieldHump -> 2
            else -> 1
        }
        for (element in elements) {
            if (element.isEnable) {
                // 设置类型
                element.fieldNameType = type
                // 置空
                element.fieldName = ""
            }
        }
        refreshJScrollPane()
    }
}
