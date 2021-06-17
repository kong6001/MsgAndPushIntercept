package com.alguojian.msgandpushintercept.service

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils


class ComeMessage(private val myMessage: IComeMessage, private val context: Context) {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            val packageName = bundle!!.getString("packageName")
            val msg = bundle.getString("content")

            myMessage.allMessage(msg)
            //            if (packageName.contains(WX)) {
            //                myMessage.comeWxMessage(msg);
            //            } else if (packageName.contains(QQ)) {
            //                myMessage.comeQQmessage(msg);
            //            } else if (packageName.contains(MMS)) {
            //                myMessage.comeShortMessage(msg);
            //            }
        }
    }

    val isEnabled: Boolean
        get() {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                ENABLED_NOTIFICATION_LISTENERS
            )
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in names.indices) {
                    val cn = ComponentName.unflattenFromString(names[i])
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    init {
        registBroadCast()
    }

    private fun registBroadCast() {
        val filter = IntentFilter(NotifyService.SEND_MSG_BROADCAST)
        context.registerReceiver(receiver, filter)
    }

    fun unRegistBroadcast() {
        context.unregisterReceiver(receiver)
    }

    fun openSetting() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun toggleNotificationListenerService() {
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(context, NotifyService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(context, NotifyService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    companion object {
        private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        val QQ = "com.tencent.mobileqq"
        val WX = "com.tencent.mm"
        val MMS = "com.android.mms"
    }
}
