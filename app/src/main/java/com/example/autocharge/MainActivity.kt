package com.example.autocharge


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView
    private lateinit var webSocketClient: MyWebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        // 启动 WebSocket 客户端
        startWebSocketClient()

        // 设置定时任务（如果需要）
        setupPeriodicWork()
    }

    private fun setupPeriodicWork() {
        // 设置定时任务（例如每 30 分钟检查一次）
        val workRequest = PeriodicWorkRequest.Builder(
            AppCheckWorker::class.java,
            30, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun startWebSocketClient() {
        // 初始化 WebSocket 客户端
        webSocketClient = MyWebSocketClient(this)

        // 连接到 WebSocket 服务器
        val serverUrl = "ws://47.97.50.103:26521/orderResult" // 替换为你的 WebSocket 服务器地址
        webSocketClient.connect(serverUrl)

        // 发送测试消息（可选）
        webSocketClient.sendMessage("Hello, Server!")
    }

    // 日志记录方法
    fun log(message: String) {
        runOnUiThread {
            logTextView.append("$message\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭 WebSocket 连接
        webSocketClient.close()
    }
}