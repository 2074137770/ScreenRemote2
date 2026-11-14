package com.example.screenremote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.media.AudioManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class SwipeAccessibilityService : AccessibilityService() {

    companion object {
        var instance: SwipeAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        ControlServer.start(9090) // 这里端口改成了 9090
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    // 滑动（上/下）
    fun swipe(direction: String) {
        val dm = resources.displayMetrics
        val w = dm.widthPixels
        val h = dm.heightPixels
        val x = w / 2f
        
        // 拉长滑动距离：从 80% 滑到 20%，确保视频 App 能识别为翻页
        val startY = if (direction == "up") h * 0.8f else h * 0.2f
        val endY = if (direction == "up") h * 0.2f else h * 0.8f

        val path = Path().apply {
            moveTo(x, startY)
            lineTo(x, endY)
        }
        
        // 放慢滑动时间：从 200 毫秒改成 300 毫秒，太快了系统会当成点击
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        
        dispatchGesture(gesture, null, null)
    }

    // 双击点赞
    fun doubleTapLike() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val y = dm.heightPixels / 2f

        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        
        android.os.Handler(mainLooper).postDelayed({
            dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        }, 100)
    }

    // 点击打开评论
    fun openComment() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val y = dm.heightPixels * 0.85f

        val path = Path().apply { moveTo(x, y) }
        dispatchGesture(GestureDescription.Builder().addStroke(
            GestureDescription.StrokeDescription(path, 0, 50)
        ).build(), null, null)
    }

    // 调节音量
    fun changeVolume(up: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    // 调节亮度
    fun changeBrightness(up: Boolean) {
        try {
            val currentBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            val newBrightness = if (up) (currentBrightness + 30).coerceAtMost(255) else (currentBrightness - 30).coerceAtLeast(10)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
