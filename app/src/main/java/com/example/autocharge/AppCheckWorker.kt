package com.example.autocharge

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class AppCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("AppCheckWorker", "Checking if app is running in background...")

        // 检测应用是否在后台运行
        if (!isAppRunning()) {
            Log.d("AppCheckWorker", "App is not running, starting app...")
            // 启动应用
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        } else {
            Log.d("AppCheckWorker", "App is already running.")
        }

        return Result.success()
    }

    private fun isAppRunning(): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningTasks = activityManager.getRunningTasks(100) // 获取最近运行的 100 个任务

        for (task in runningTasks) {
            if (task.baseActivity?.packageName == applicationContext.packageName) {
                return true
            }
        }
        return false
    }
}