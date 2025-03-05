package com.example.autocharge

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import okhttp3.*

class MyWebSocketClient(private val context: Context) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // 启动 WebSocket 连接
    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        val listener = EchoWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    // 发送消息
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    // 关闭连接
    fun close() {
        webSocket?.close(1000, "Goodbye!")
    }

    // WebSocket 监听器
    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            // WebSocket 连接成功
            Log.d("WebSocket", "Connected to server")
            (context as MainActivity).log("WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // 收到服务器消息
            Log.d("WebSocket", "Received: $text")
            (context as MainActivity).log("Received WebSocket message: $text")



            if (text.startsWith("number:")) {
                val number = text.substringAfter("number:")
                wakeUpDevice()
                openChargerApp(number)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            // 服务器关闭连接
            Log.d("WebSocket", "Closing: $reason")
            (context as MainActivity).log("WebSocket closed: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // 连接失败
            Log.e("WebSocket", "Error: ${t.message}")
            (context as MainActivity).log("WebSocket error: ${t.message}")
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
}