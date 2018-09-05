import com.intellij.notification.*
import com.pingfangx.plugin.generatex.constant.Constant
import java.util.*


/**
 * 首字母大写
 */
fun String.firstToUpperCase() = this.substring(0, 1).toUpperCase(Locale.CHINA) + this.substring(1)

/**
 * 输出到Log窗口
 */
fun String.outInfo() {
    NotificationsConfiguration.getNotificationsConfiguration().register(Constant.GENERATE_X, NotificationDisplayType.NONE)
    Notifications.Bus.notify(Notification(Constant.GENERATE_X, "${Constant.GENERATE_X} [INFO]", this, NotificationType.INFORMATION))
}

/**
 * layout.getValue()返回的值为@layout/layout_view
 * @return String
 */
fun String?.getLayoutName(): String? {
    if (this == null || !this.startsWith("@") || !this.contains("/")) {
        return null
    }

    // @layout layout_view
    val parts = this.split("/".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
    if (parts.size != 2) {
        return null
    }
    // layout_view
    return parts[1]
}