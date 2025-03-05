package com.example.autocharge

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*

class MyWebSocketClient(private val context: Context) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val sharedPreferences by lazy {
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        val listener = EchoWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Goodbye!")
    }

    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to server")
            (context as MainActivity).log("WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")
            //(context as MainActivity).log("Received WebSocket message: $text")

            // 获取密码
            val password = sharedPreferences.getString("password", null)

            // 检查消息格式
            if (text.length != 14 || !text.matches(Regex("\\d{14}"))) {
                //(context as MainActivity).log("Invalid message format: $text")
                return
            }

            // 检查前 6 位是否与密码匹配
            if (password == null || text.substring(0, 6) != password) {
                //(context as MainActivity).log("Password mismatch or not set")
                return
            }

            // 检查后 8 位是否为数字
            val data = text.substring(6)
            if (!data.matches(Regex("\\d{8}"))) {
                (context as MainActivity).log("有人输入了你的密码想要充电，但是充电桩序列号不对 [${getCurrentTimestamp()}]")
                return
            }

            // 所有条件满足，执行操作
            (context as MainActivity).log("有人输入了你的密码想要充电 [${getCurrentTimestamp()}]")
            wakeUpDevice()
            openChargerApp(data)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Closing: $reason")
            (context as MainActivity).log("WebSocket closed: $reason [${getCurrentTimestamp()}]")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}")
            (context as MainActivity).log("WebSocket error: ${t.message} [${getCurrentTimestamp()}]")
        }
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

    // 获取当前时间戳
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}