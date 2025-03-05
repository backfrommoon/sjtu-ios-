package com.example.autocharge

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.Worker
import androidx.work.WorkerParameters

class AppCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        // 检测应用是否在后台运行
        if (!isAppRunning(context)) {
            // 启动应用
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        // 检测无障碍权限
        if (!isAccessibilityServiceEnabled(context)) {
            // 提示用户开启无障碍权限
            val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            accessibilityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(accessibilityIntent)
        }

        return Result.success()
    }

    private fun isAppRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false

        for (processInfo in runningProcesses) {
            if (processInfo.processName == context.packageName) {
                return true
            }
        }
        return false
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains("com.example.autocharge.ChargerAccessibilityService") == true
    }
}