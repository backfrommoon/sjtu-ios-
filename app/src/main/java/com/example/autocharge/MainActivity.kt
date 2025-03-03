package com.example.autocharge

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.ServerSocket

class MainActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView
    private lateinit var server: MyHttpServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        // 启动HTTP服务器
        startHttpServer()
    }

    private fun startHttpServer() {
        // 获取设备的局域网IP地址
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        log("Device IP: $ipAddress")

        // 动态选择一个可用端口
        val availablePort = findAvailablePort(8080)
        server = MyHttpServer(availablePort, this)
        server.start()
        log("HTTP server started on port $availablePort")
    }

    private fun findAvailablePort(startPort: Int): Int {
        var port = startPort
        while (port < 65535) {
            try {
                val serverSocket = ServerSocket(port)
                serverSocket.close()
                return port
            } catch (e: IOException) {
                port++
            }
        }
        throw IOException("No available port found")
    }

    fun log(message: String) {
        runOnUiThread {
            logTextView.append("$message\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止HTTP服务器
        server.stop()
    }
}