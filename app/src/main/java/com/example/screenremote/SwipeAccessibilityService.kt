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
        ControlServer.start(9090)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    
    override fun onInterrupt() {}

    fun executeCommand(cmd: String) {
        if (cmd == "UP") {
            swipe("up")
        } else if (cmd == "DOWN") {
            swipe("down")
        } else if (cmd == "Z") {
            tap(0.5f, 0.5f) // 点赞：点屏幕正中
        } else if (cmd == "X") {
            tap(0.85f, 0.85f) // 评论：点右下角
        } else if (cmd == "C") {
            tap(0.85f, 0.65f) // 收藏：点右侧中下
        } else if (cmd == "VOL_UP") {
            changeVolume(true)
        } else if (cmd == "VOL_DOWN") {
            changeVolume(false)
        } else if (cmd == "BRIGHT_UP") {
            changeBrightness(true)
        } else if (cmd == "BRIGHT_DOWN") {
            changeBrightness(false)
        }
    }

    private fun swipe(direction: String) {
        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels
        val x = w / 2f
        var startY = h * 0.8f
        var endY = h * 0.2f
        
        if (direction != "up") {
            startY = h * 0.2f
            endY = h * 0.8f
        }

        val path = Path()
        path.moveTo(x, startY)
        path.lineTo(x, endY)

        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        dispatchGesture(builder.build(), null, null)
    }

    private fun tap(xPercent: Float, yPercent: Float) {
        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels
        val x = w * xPercent
        val y = h * yPercent

        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        dispatchGesture(builder.build(), null, null)
    }

    private fun changeVolume(up: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        var direction = AudioManager.ADJUST_LOWER
        if (up) {
            direction = AudioManager.ADJUST_RAISE
        }
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    private fun changeBrightness(up: Boolean) {
        try {
            val current = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            var target = current - 30
            if (up) {
                target = current + 30
            }
            if (target > 255) target = 255
            if (target < 10) target = 10
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, target)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
