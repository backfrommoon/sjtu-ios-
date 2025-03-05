package com.example.autocharge


import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import fi.iki.elonen.NanoHTTPD

class MyHttpServer(port: Int, private val context: Context) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val number = session.parms["number"]

        if (number != null) {
            (context as MainActivity).log("Received number: $number")

            // 检测屏幕状态并唤醒屏幕
            wakeUpDevice()

            // 打开充电App并传递号码
            openChargerApp(number)

            return newFixedLengthResponse("Number received: $number")
        }

        return newFixedLengthResponse("No number received")
    }

    private fun wakeUpDevice() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp::WakeLockTag"
        )
        wakeLock.acquire(5000) // 保持唤醒5秒

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("MyApp::KeyguardLock")
        keyguardLock.disableKeyguard() // 解锁屏幕
    }

    private fun openChargerApp(number: String) {
        val intent = Intent(context, ChargerActivity::class.java)
        intent.putExtra("number", number)
        context.startActivity(intent)
    }
}