package com.alguojian.msgandpushintercept

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alguojian.msgandpushintercept.service.ComeMessage
import com.alguojian.msgandpushintercept.service.IComeMessage
import com.alguojian.msgandpushintercept.utils.PhoneCallUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 获得推送通知消息
 *
 * @author alguojian
 * @date 2019/07/24
 */
class MainActivity : AppCompatActivity(), IComeMessage {
    override fun allMessage(msg: String?) {
        mAdapter.addData(msg)
    }

    private lateinit var telephonyManager: TelephonyManager

    private lateinit var callListener: PhoneCallListener
    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1000)
//        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        callListener = PhoneCallListener()

        floatbutton.setOnClickListener {
            val comeMessage = ComeMessage(this@MainActivity, this@MainActivity)
            if (!comeMessage.isEnabled) {
                comeMessage.openSetting()
                comeMessage.toggleNotificationListenerService()
            }
        }

        tv_phone.setOnClickListener {
            telephonyManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        mAdapter = MyAdapter(this@MainActivity)
        recyclerView.adapter = mAdapter
    }


    /**
     * 监听来电状态
     */
    inner class PhoneCallListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            when (state) {
                //电话通话的状态
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                }
                //电话响铃的状态
                TelephonyManager.CALL_STATE_RINGING -> PhoneCallUtil.endPhone(this@MainActivity)
            }
            super.onCallStateChanged(state, incomingNumber)
        }
    }
}
