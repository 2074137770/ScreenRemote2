package com.example.screenremote

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: FrameLayout
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // MIUIX 经典配色
    private val bgColor = Color.parseColor("#F2F3F5")
    private val cardColor = Color.WHITE
    private val primaryColor = Color.parseColor("#3482FF") // MIUI 蓝
    private val textColorMain = Color.parseColor("#000000")
    private val textColorSub = Color.parseColor("#666666")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootLayout = FrameLayout(this).apply {
            setBackgroundColor(bgColor)
        }
        setContentView(rootLayout)
        showHome()
    }

    // 通用圆角卡片生成器
    private fun createCard(cornerRadiusDp: Int = 24): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable().apply {
                setColor(cardColor)
                cornerRadius = dp(cornerRadiusDp).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
        }
    }

    // ================== 页面 1：主页 ==================
    private fun showHome() {
        rootLayout.removeAllViews()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "灵滑"
            textSize = 36f
            setTextColor(textColorMain)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "滑动一下，掌控全场"
            textSize = 16f
            setTextColor(textColorSub)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(60))
        }

        val btnController = createMenuButton("我是控制端", "用手机控制平板") { showControllerInput() }
        val btnReceiver = createMenuButton("我是被控端", "用平板接收指令") { showReceiver() }

        container.addView(title)
        container.addView(subtitle)
        container.addView(btnController)
        container.addView(btnReceiver)
        rootLayout.addView(container)
    }

    private fun createMenuButton(title: String, sub: String, onClick: () -> Unit): LinearLayout {
        val card = createCard()
        card.isClickable = true
        card.isFocusable = true
        // MIUI 点击水波纹
        val ripple = GradientDrawable().apply {
            setColor(Color.parseColor("#1A3482FF"))
            cornerRadius = dp(24).toFloat()
        }
        card.foreground = ripple

        val tvTitle = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(textColorMain)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tvSub = TextView(this).apply {
            text = sub
            textSize = 14f
            setTextColor(textColorSub)
            setPadding(0, dp(4), 0, 0)
        }
        card.addView(tvTitle)
        card.addView(tvSub)
        card.setOnClickListener { onClick() }
        return card
    }

    // ================== 页面 2：被控端 ==================
    private fun showReceiver() {
        rootLayout.removeAllViews()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(16))
        }

        val ip = getLocalIpAddress()

        val ipCard = createCard()
        val tvIpTitle = TextView(this).apply {
            text = "当前设备 IP"
            textSize = 14f
            setTextColor(textColorSub)
        }
        val tvIp = TextView(this).apply {
            text = ip
            textSize = 32f
            setTextColor(textColorMain)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dp(8), 0, 0)
        }
        ipCard.addView(tvIpTitle)
        ipCard.addView(tvIp)

        val permCard = createCard()
        val tvPerm = TextView(this).apply {
            text = "核心权限"
            textSize = 16f
            setTextColor(textColorMain)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tvPermSub = TextView(this).apply {
            text = "开启无障碍服务，用于模拟滑动和点赞等操作"
            textSize = 14f
            setTextColor(textColorSub)
            setPadding(0, dp(4), 0, dp(16))
        }
        val btnPerm = TextView(this).apply {
            text = "去开启"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = GradientDrawable().apply {
                setColor(primaryColor)
                cornerRadius = dp(16).toFloat()
            }
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        permCard.addView(tvPerm)
        permCard.addView(tvPermSub)
        permCard.addView(btnPerm)

        val backBtn = TextView(this).apply {
            text = "← 返回主页"
            textSize = 16f
            setTextColor(primaryColor)
            setPadding(dp(16), dp(24), 0, 0)
            setOnClickListener { showHome() }
        }

        container.addView(ipCard)
        container.addView(permCard)
        container.addView(backBtn)
        rootLayout.addView(container)
    }

    // ================== 页面 3：控制端输入 IP ==================
    private fun showControllerInput() {
        rootLayout.removeAllViews()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), 0, dp(16), 0)
        }

        val inputCard = createCard()
        val tvTitle = TextView(this).apply {
            text = "连接设备"
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(textColorMain)
        }
        val etIp = EditText(this).apply {
            hint = "输入平板上的 IP 地址"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
            setPadding(0, dp(16), 0, dp(16))
            textSize = 18f
        }
        val btnConnect = TextView(this).apply {
            text = "连接"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(14))
            background = GradientDrawable().apply {
                setColor(primaryColor)
                cornerRadius = dp(16).toFloat()
            }
            setOnClickListener {
                val ip = etIp.text.toString().trim()
                if (ip.isNotEmpty()) connectToHost(ip) else Toast.makeText(this@MainActivity, "请输入 IP", Toast.LENGTH_SHORT).show()
            }
        }
        inputCard.addView(tvTitle)
        inputCard.addView(etIp)
        inputCard.addView(btnConnect)

        val backBtn = TextView(this).apply {
            text = "← 返回主页"
            textSize = 16f
            setTextColor(primaryColor)
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, 0)
            setOnClickListener { showHome() }
        }

        container.addView(inputCard)
        container.addView(backBtn)
        rootLayout.addView(container)
    }

    // ================== 页面 4：控制端遥控面板 ==================
    private fun showControllerPanel() {
        rootLayout.removeAllViews()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(16))
        }

        val statusText = TextView(this).apply {
            text = "已连接 🟢"
            textSize = 16f
            setTextColor(Color.parseColor("#00C853"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        }

        // 滑动触控区
        val padCard = createCard()
        padCard.gravity = Gravity.CENTER
        padCard.layoutParams.height = dp(240)
        val tvPad = TextView(this).apply {
            text = "在此区域上下滑动\n(控制视频切换)"
            textSize = 16f
            setTextColor(textColorSub)
            gravity = Gravity.CENTER
        }
        padCard.addView(tvPad)

        var startY = 0f
        padCard.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { startY = event.y; true }
                MotionEvent.ACTION_UP -> {
                    val dy = event.y - startY
                    if (Math.abs(dy) > 50) { // 滑动阈值
                        if (dy < 0) sendCommand("up") else sendCommand("down")
                    }
                    true
                }
                else -> true
            }
        }

        // 快捷按钮区
        val btnRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(8), 0, dp(8))
        }
        val btnLike = createActionBtn("❤️ 点赞") { sendCommand("like") }
        val btnComment = createActionBtn("💬 评论") { sendCommand("comment") }
        btnRow1.addView(btnLike)
        btnRow1.addView(btnComment)

        val btnRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(8), 0, dp(8))
        }
        val btnVolUp = createActionBtn("🔊 音量+") { sendCommand("vol_up") }
        val btnVolDown = createActionBtn("🔉 音量-") { sendCommand("vol_down") }
        btnRow2.addView(btnVolUp)
        btnRow2.addView(btnVolDown)

        val btnRow3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(8), 0, dp(8))
        }
        val btnBrightUp = createActionBtn("☀️ 亮度+") { sendCommand("bright_up") }
        val btnBrightDown = createActionBtn("🌙 亮度-") { sendCommand("bright_down") }
        btnRow3.addView(btnBrightUp)
        btnRow3.addView(btnBrightDown)

        val backBtn = TextView(this).apply {
            text = "断开并返回"
            textSize = 16f
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, 0)
            setOnClickListener {
                try { socket?.close() } catch (e: Exception) {}
                showHome()
            }
        }

        container.addView(statusText)
        container.addView(padCard)
        container.addView(btnRow1)
        container.addView(btnRow2)
        container.addView(btnRow3)
        container.addView(backBtn)
        rootLayout.addView(container)
    }

    private fun createActionBtn(text: String, onClick: () -> Unit): LinearLayout {
        val card = createCard(cornerRadiusDp = 16)
        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        params.setMargins(dp(4), 0, dp(4), 0)
        card.layoutParams = params
        card.gravity = Gravity.CENTER
        card.isClickable = true
        val tv = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(textColorMain)
        }
        card.addView(tv)
        card.setOnClickListener { onClick() }
        return card
    }

    // ================== 网络通信 ==================
    private fun connectToHost(ip: String) {
        Thread {
            try {
                socket = Socket(ip, 9090)
                writer = PrintWriter(socket?.getOutputStream(), true)
                mainHandler.post { showControllerPanel() }
            } catch (e: Exception) {
                mainHandler.post { Toast.makeText(this@MainActivity, "连接失败，请检查 IP 和网络", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }

    private fun sendCommand(cmd: String) {
        Thread {
            try {
                writer?.println(cmd)
            } catch (e: Exception) {
                mainHandler.post { Toast.makeText(this@MainActivity, "发送失败，连接已断开", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    // ================== 工具方法 ==================
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "未知"
                    }
                }
            }
        } catch (_: Exception) {}
        return "未知"
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
