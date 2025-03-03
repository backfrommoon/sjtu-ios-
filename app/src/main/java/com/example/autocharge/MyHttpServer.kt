package com.example.autocharge
import android.content.Context
import android.content.Intent
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*


class MyHttpServer(port: Int, private val context: Context) : NanoHTTPD(port) {
    private fun delay_5scds() {
        GlobalScope.launch {
            println("开始...")
            delay(5000) // 延迟 5 秒
            println("5 秒后执行")
        }
    }
    override fun serve(session: IHTTPSession): Response {
        // 获取请求参数
        val number = session.parms["number"]

        if (number != null) {
            // 记录日志
            (context as MainActivity).log("Received number: $number")
            //delay_5scds()
            // 打开充电App并传递号码
            openChargerApp(number)

            // 返回响应
            return newFixedLengthResponse("Number received: $number")
        }

        return newFixedLengthResponse("No number received")
    }

    private fun openChargerApp(number: String) {
        // 打开充电App（假设包名为com.example.charger）
        val intent = Intent(context, ChargerActivity::class.java)
        intent.putExtra("number", number) // 传递号码
        context.startActivity(intent)
    }
}