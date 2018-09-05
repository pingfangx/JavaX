package com.pingfangx.plugin.generatex.constant

/**
 * @author Jowan
 */
object Constant {
    const val GENERATE_X = "GenerateX"
    /**
     * 弹窗时间
     */
    const val POPUP_TIME = 5

    object Action {
        const val SELECTED_TEXT_ACTIVITY_PREFIX = "activity_"
        const val SELECTED_TEXT_SUFFIX = ".xml"
        const val SELECTED_MESSAGE = "布局内容：（不需要输入R.layout.）"
        const val SELECTED_TITLE = "未选中布局内容，请输入layout文件名"
        const val SELECTED_ERROR_NO_NAME = "未输入layout文件名"
        const val SELECTED_ERROR_NO_SELECTED = "未找到选中的布局文件"
        const val SELECTED_ERROR_NO_ID = "未找到任何Id"
        const val SELECTED_ERROR_NO_POINT = "光标未在Class内"
        const val SELECTED_SUCCESS = "生成成功"
    }

    object Dialog {
        const val TITLE = "FindViewByIdX"
        const val TABLE_FIELD_VIEW_WIDGET = "ViewWidget"
        const val TABLE_FIELD_VIEW_ID = "ViewId"
        const val FIELD_ON_CLICK = "OnClick"
        const val FIELD_INIT_METHOD = "init method"
        const val BUTTON_CONFIRM = "确定"
        const val BUTTON_CANCEL = "取消"
    }

    object Ext {
        const val UNKNOWN_ERROR = "Unknown Error"
    }

}
