package com.example.autocharge

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*

class MyWebSocketClient(private val context: Context) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var lastPingTime: Long = 0 // 记录最后一次收到 "ping" 的时间
    private val pingCheckInterval = 2 * 60 * 1000L // 2分钟
    private val handler = Handler(Looper.getMainLooper())
    private val pingCheckRunnable = object : Runnable {
        override fun run() {
            checkConnection()
            handler.postDelayed(this, pingCheckInterval) // 每2分钟检查一次
        }
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        val listener = EchoWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
        handler.post(pingCheckRunnable) // 启动定时检查
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Goodbye!")
        handler.removeCallbacks(pingCheckRunnable) // 停止定时检查
    }

    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to server")
            (context as MainActivity).log("WebSocket opened")
            lastPingTime = System.currentTimeMillis() // 连接成功后记录时间
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")
            //(context as MainActivity).log("Received WebSocket message: $text")

            // 处理 "ping" 消息
            if (text == "ping") {
                webSocket.send("pong")
                lastPingTime = System.currentTimeMillis() // 更新最后一次收到 "ping" 的时间
                return
            }

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
            handler.removeCallbacks(pingCheckRunnable) // 停止定时检查
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}")
            (context as MainActivity).log("WebSocket error: ${t.message} [${getCurrentTimestamp()}]")
            handler.removeCallbacks(pingCheckRunnable) // 停止定时检查
            reconnect() // 尝试重新连接
        }
    }

    private fun checkConnection() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPingTime > pingCheckInterval) {
            // 超过2分钟没有收到 "ping"，检查连接状态
            if (webSocket == null) {
                reconnect()
            } else {
                // 主动发送 "ping" 检测连接
                webSocket?.send("ping")
            }
        }
    }

    private fun reconnect() {
        Log.d("WebSocket", "Attempting to reconnect...")
        (context as MainActivity).log("Attempting to reconnect... [${getCurrentTimestamp()}]")
        close() // 关闭现有连接
        connect("ws://47.97.50.103:26521/orderResult") // 重新连接，替换为你的 WebSocket URL
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