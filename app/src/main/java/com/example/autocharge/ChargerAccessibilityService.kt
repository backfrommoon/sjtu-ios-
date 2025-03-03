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

//                // 点击扫码充电
//                clickButtonById(rootNode, "com.dses.campuslife:id/button_charge_list")
//
//                // 点击输入框
//                clickButtonByText(rootNode, "输入")
//
//                // 输入设备号
//                val deviceNumber = getDeviceNumber() // 从 SharedPreferences 获取设备号
//                enterTextInField(rootNode, "com.dses.campuslife:id/dialog_input_input", deviceNumber)
                // 点击确定
                //clickButtonByText(rootNode, "确定")
            }, 5000) // 延迟5秒，确保App已经完全加载
            Handler(Looper.getMainLooper()).postDelayed({
                val rootNode = rootInActiveWindow ?: return@postDelayed

                // 点击扫码充电
                clickButtonById(rootNode, "com.dses.campuslife:id/button_charge_list")

                // 点击输入框
                clickButtonByText(rootNode, "输入")

                // 输入设备号
                val deviceNumber = getDeviceNumber() // 从 SharedPreferences 获取设备号
                enterTextInField(rootNode, "com.dses.campuslife:id/dialog_input_input", deviceNumber)
                // 点击确定
                //clickButtonByText(rootNode, "确定")
            }, 1000)
            Handler(Looper.getMainLooper()).postDelayed({
                val rootNode = rootInActiveWindow ?: return@postDelayed

                // 输入设备号
                val deviceNumber = getDeviceNumber() // 从 SharedPreferences 获取设备号
                enterTextInField(rootNode, "com.dses.campuslife:id/dialog_input_input", deviceNumber)
                // 点击确定
                //clickButtonByText(rootNode, "确定")
            }, 2000) // 延迟5秒，确保App已经完全加载

        }
    }

    private fun getDeviceNumber(): String {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("device_data", MODE_PRIVATE)
        return sharedPreferences.getString("device_number", "default_number") ?: "default_number"
    }

    private fun enterTextInField(rootNode: AccessibilityNodeInfo, id: String, text: String) {
        Log.d("ChargerAccessibility", "Attempting to find input field with ID: $id")
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
        if (nodes.isEmpty()) {
            Log.d("ChargerAccessibility", "No input field found with ID: $id")
        } else {
            Log.d("ChargerAccessibility", "Input field found, attempting to enter text: $text")
            for (node in nodes) {
                // 直接设置文本
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                Log.d("ChargerAccessibility", "输入文本: $text")

                // 延迟 3 秒后点击确定按钮
                Handler(Looper.getMainLooper()).postDelayed({
                    clickButtonByText(rootNode, "确定")
                    Log.d("ChargerAccessibility", "延迟 3 秒后点击‘确定’按钮")
                }, 1000) // 延迟 3 秒
            }
        }
    }

    // 模拟按键输入
    private fun performKeyPress(node: AccessibilityNodeInfo, char: Char) {
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, char.toString())
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
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
