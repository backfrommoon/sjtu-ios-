package com.example.autocharge

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.Toast

class ChargerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Handler(Looper.getMainLooper()).postDelayed({
                val rootNode = rootInActiveWindow ?: return@postDelayed

                // 点击智能充电
                clickButtonByText_parent(rootNode, "智能充电")

                // 点击扫码充电
                clickButtonById(rootNode, "com.dses.campuslife:id/button_charge_list")

                // 点击输入框
                clickButtonByText(rootNode, "输入")

                // 输入设备号
                val deviceNumber = getDeviceNumber() // 从 SharedPreferences 获取设备号
                enterTextInField(rootNode, "com.dses.campuslife:id/input_device", deviceNumber)

                // 点击确定
                //clickButtonByText(rootNode, "确定")
            }, 5000) // 延迟5秒，确保App已经完全加载
        }
    }

    private fun getDeviceNumber(): String {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("device_data", MODE_PRIVATE)
        return sharedPreferences.getString("device_number", "default_number") ?: "default_number"
    }

    private fun enterTextInField(rootNode: AccessibilityNodeInfo, id: String, text: String) {
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
        for (node in nodes) {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

            // 在输入设备号时显示 Toast
            Toast.makeText(applicationContext, "正在输入：$text", Toast.LENGTH_SHORT).show()
            Log.d("ChargerAccessibility", "输入设备号: $text")
        }
    }

    private fun clickButtonByText(rootNode: AccessibilityNodeInfo, text: String) {
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("ChargerAccessibility", "点击了‘$text’ 按钮")
        }
    }

    private fun clickButtonByText_parent(rootNode: AccessibilityNodeInfo, text: String) {
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("ChargerAccessibility", "点击了‘$text’ 按钮")
                    return
                }
                parent = parent.parent
            }
        }
    }

    private fun clickButtonById(rootNode: AccessibilityNodeInfo, id: String) {
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("ChargerAccessibility", "点击了‘$id’ 按钮")
            }
        }
    }

    override fun onInterrupt() {}
}
