package com.example.autocharge

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChargerActivity : AppCompatActivity() {

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (!TextUtils.isEmpty(enabledServices)) {
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.contains(service.name)) {
                    return true
                }
            }
        }
        return false
    }

    private fun startAccessibilityService() {
        if (isAccessibilityServiceEnabled(this, ChargerAccessibilityService::class.java)) {
            Toast.makeText(this, "无障碍服务已开启，直接启动校园生活", Toast.LENGTH_SHORT).show()
            startcampuslife()
        } else {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "请开启无障碍服务以自动点击‘智能充电’按钮", Toast.LENGTH_LONG).show()
        }
    }

    fun startcampuslife(){
        if (!isAccessibilityServiceEnabled(this, ChargerAccessibilityService::class.java)) {
            startAccessibilityService()
        } else {
            val intent0 = packageManager.getLaunchIntentForPackage("com.dses.campuslife")
            val intent = Intent()
            intent.setComponent(ComponentName("com.dses.campuslife", "com.dses.campuslife.activity.MainActivity"))
            if (intent0 != null) {
                intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent0)
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "校园生活 App 未安装", Toast.LENGTH_SHORT).show()
            }
            // 创建 Intent 来启动特定的活动


        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charger)

        val numberTextView: TextView = findViewById(R.id.numberTextView)

        // 获取传递的号码
        val number = intent?.getStringExtra("number")
        if (number != null) {
            numberTextView.text = "Received number: $number"
            // 保存设备号到 SharedPreferences
            saveDeviceNumber(number)
            // 先打开校园生活app
            startcampuslife()
        } else {
            numberTextView.text = "No number received"
        }
    }
    private fun saveDeviceNumber(number: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("device_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("device_number", number)
        editor.apply()
    }
}
