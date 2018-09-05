package com.pingfangx.plugin.generatex.entity

import java.awt.LayoutManager
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * @author Jowan
 */
class IdBean
(layout: LayoutManager,
 emptyBorder: EmptyBorder,
 private val mEnableCheckBox: JCheckBox,
 private val mIdJLabel: JLabel,
 private val mClickCheckBox: JCheckBox,
 private val mFieldJTextField: JTextField,
 enable: Boolean,
 clickable: Boolean,
 clickEnable: Boolean) : JPanel(layout) {

    /**
     * mEnableCheckBox接口
     */
    private var mEnableListener: ((enableCheckBox: JCheckBox) -> Unit)? = null

    fun setEnableActionListener(enableListener: (enableCheckBox: JCheckBox) -> Unit) {
        mEnableListener = enableListener
    }

    /**
     * mFieldJTextField接口
     */
    private var mFieldFocusListener: ((fieldJTextField: JTextField) -> Unit)? = null

    fun setFieldFocusListener(fieldFocusListener: (fieldJTextField: JTextField) -> Unit) {
        mFieldFocusListener = fieldFocusListener
    }

    /**
     * mClickCheckBox接口
     */
    private var mClickListener: ((clickCheckBox: JCheckBox) -> Unit)? = null

    fun setClickActionListener(clickListener: (clickCheckBox: JCheckBox) -> Unit) {
        mClickListener = clickListener
    }

    init {
        initLayout(layout, emptyBorder)
        initComponent(enable, clickable, clickEnable)
        addComponent()
    }

    /**
     * addComponent
     */
    private fun addComponent() {
        this.add(mEnableCheckBox)
        this.add(mIdJLabel)
        this.add(mClickCheckBox)
        this.add(mFieldJTextField)
    }

    /**
     * 设置Component

     * @param enable enable
     *
     * @param clickable clickable
     *
     * @param clickEnable clickEnable
     */
    private fun initComponent(enable: Boolean, clickable: Boolean, clickEnable: Boolean) {
        mEnableCheckBox.isSelected = enable
        if (clickEnable) {
            mClickCheckBox.isSelected = clickable
            mClickCheckBox.isEnabled = enable
        } else {
            mClickCheckBox.isEnabled = false
        }

        mIdJLabel.isEnabled = enable
        mFieldJTextField.isEnabled = enable

        // 设置左对齐
        mEnableCheckBox.horizontalAlignment = JLabel.LEFT
        mIdJLabel.horizontalAlignment = JLabel.LEFT
        mFieldJTextField.horizontalAlignment = JTextField.LEFT
        // 监听
        mEnableCheckBox.addActionListener {
            mEnableListener?.let {
                mEnableListener?.invoke(mEnableCheckBox)
                mIdJLabel.isEnabled = mEnableCheckBox.isSelected
                if (clickEnable) mClickCheckBox.isEnabled = mEnableCheckBox.isSelected
                mFieldJTextField.isEnabled = mEnableCheckBox.isSelected
            }
        }
        // 监听
        mClickCheckBox.addActionListener {
            mClickListener?.invoke(mClickCheckBox)
        }
        // 监听
        mFieldJTextField.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent) {
                mFieldFocusListener?.invoke(mFieldJTextField)
            }

            override fun focusLost(e: FocusEvent) {
                mFieldFocusListener?.invoke(mFieldJTextField)
            }
        })
    }

    /**
     * 设置布局相关

     * @param layout layout
     *
     * @param emptyBorder emptyBorder
     */
    private fun initLayout(layout: LayoutManager, emptyBorder: EmptyBorder) {
        // 设置布局内容
        this.layout = layout
        // 设置border
        this.border = emptyBorder
    }
}
