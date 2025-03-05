package com.example.autocharge

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var passwordTextView: TextView
    private lateinit var logTextView: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var webSocketClient: MyWebSocketClient

    private val sharedPreferences by lazy {
        getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        passwordTextView = findViewById(R.id.passwordTextView)
        logTextView = findViewById(R.id.logTextView)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        // 检查并显示密码
        checkAndDisplayPassword()

        // 设置修改密码按钮的点击事件
        changePasswordButton.setOnClickListener {
            showPasswordInputDialog()
        }

        // 检查无障碍权限
        checkAccessibilityPermission()

        // 启动 WebSocket 客户端
        startWebSocketClient()

        // 设置定时任务（如果需要）
        setupPeriodicWork()
    }

    private fun checkAndDisplayPassword() {
        val password = sharedPreferences.getString("password", null)
        if (password == null || password.length != 6 || !password.matches(Regex("\\d{6}"))) {
            passwordTextView.text = "Password: Not Set (Invalid)"
            passwordTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            showPasswordInputDialog()
        } else {
            passwordTextView.text = "Password: $password"
            passwordTextView.setTextColor(resources.getColor(android.R.color.black))
        }
    }

    private fun showPasswordInputDialog() {
        val inputDialog = AlertDialog.Builder(this)
        inputDialog.setTitle("Set Password")
        inputDialog.setMessage("Enter a 6-digit numeric password:")

        // 使用 EditText 作为输入控件
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER // 限制输入为数字
        inputDialog.setView(input)

        inputDialog.setPositiveButton("OK") { _, _ ->
            val password = input.text.toString()
            if (password.length == 6 && password.matches(Regex("\\d{6}"))) {
                // 保存密码到 SharedPreferences
                sharedPreferences.edit().putString("password", password).apply()
                // 更新 UI 显示密码
                passwordTextView.text = "Password: $password"
                passwordTextView.setTextColor(resources.getColor(android.R.color.black))
            } else {
                // 密码格式错误，提示用户
                Toast.makeText(this, "Invalid password! Must be 6 digits.", Toast.LENGTH_SHORT).show()
                // 重新检查并显示密码
                checkAndDisplayPassword()
            }
        }

        inputDialog.setNegativeButton("Cancel", null)
        inputDialog.show()
    }

    private fun setupPeriodicWork() {
        val workRequest = PeriodicWorkRequest.Builder(
            AppCheckWorker::class.java,
            30, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun startWebSocketClient() {
        webSocketClient = MyWebSocketClient(this)
        val serverUrl = "ws://47.97.50.103:26521/orderResult"
        webSocketClient.connect(serverUrl)
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled(this, ChargerAccessibilityService::class.java)) {
            Toast.makeText(this, "请开启无障碍服务以自动点击‘智能充电’按钮", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out android.accessibilityservice.AccessibilityService>): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (enabledServices != null) {
            val colonSplitter = enabledServices.split(':')
            for (serviceName in colonSplitter) {
                if (serviceName.contains(service.name)) {
                    return true
                }
            }
        }
        return false
    }

    fun log(message: String) {
        runOnUiThread {
            logTextView.append("$message\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}