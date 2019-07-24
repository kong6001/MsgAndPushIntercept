package com.alguojian.msgandpushintercept.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.RemoteViews

import java.lang.reflect.Field
import java.util.ArrayList
import java.util.HashMap

@SuppressLint("Registered")
class NotifyService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        val packageName = sbn.packageName
        val intent = Intent()
        intent.action = SEND_MSG_BROADCAST
        val bundle = Bundle()
        bundle.putString("packageName", packageName)
        var content: String? = null
        if (sbn.notification.tickerText != null) {
            content = sbn.notification.tickerText.toString()
        }
        if (content == null) {
            val info = getNotiInfo(sbn.notification)
            if (info != null) {
                content = info["title"].toString() + ":" + info["text"]
            }
        }
        if (content == null || content.length == 1) {
            return
        }
        intent.putExtra("content", content)
        intent.putExtras(bundle)
        this.sendBroadcast(intent)
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap
    ) {
    }

    override fun getActiveNotifications(): Array<StatusBarNotification> {
        return super.getActiveNotifications()
    }

    /**
     * 反射取通知栏信息
     *
     * @param notification
     * @return 返回短信内容
     */
    private fun getNotiInfo(notification: Notification?): Map<String, Any>? {
        var key = 0
        if (notification == null)
            return null
        val views = notification.contentView ?: return null
        val secretClass = views.javaClass

        try {
            val text = HashMap<String, Any>()

            val outerFields = secretClass.declaredFields
            for (i in outerFields.indices) {
                if (outerFields[i].name != "mActions")
                    continue

                outerFields[i].isAccessible = true

                val actions = outerFields[i].get(views) as ArrayList<Any>
                for (action in actions) {
                    val innerFields = action.javaClass.declaredFields
                    var value: Any? = null
                    var type: Int? = null
                    for (field in innerFields) {
                        field.isAccessible = true
                        if (field.name == "value") {
                            value = field.get(action)
                        } else if (field.name == "type") {
                            type = field.getInt(action)
                        }
                    }
                    // 经验所得 type 等于9 10为短信title和内容，不排除其他厂商拿不到的情况
                    if (type != null) {
                        if (key == 0) {
                            text["title"] = value?.toString() ?: ""
                        } else if (key == 1) {
                            text["text"] = value?.toString() ?: ""
                        } else {
                            text[key.toString()] = value?.toString()?:""
                        }
                        key++
                    }
                }
                key = 0

            }
            return text
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        const val SEND_MSG_BROADCAST = "SEND_MSG_BROADCAST"
    }
}
