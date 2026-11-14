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

    fun swipe(direction: String) {
        val dm = resources.displayMetrics
        val w = dm.widthPixels
        val h = dm.heightPixels
        val x = w / 2f
        val startY = if (direction == "up") h * 0.8f else h * 0.2f
        val endY = if (direction == "up") h * 0.2f else h * 0.8f

        val path = Path()
        path.moveTo(x, startY)
        path.lineTo(x, endY)

        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        val gesture = builder.build()
        
        dispatchGesture(gesture, null, null)
    }

    fun doubleTapLike() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val y = dm.heightPixels / 2f

        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        val gesture = builder.build()
        
        dispatchGesture(gesture, null, null)
        
        android.os.Handler(mainLooper).postDelayed({
            dispatchGesture(gesture, null, null)
        }, 100)
    }

    fun openComment() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val y = dm.heightPixels * 0.85f

        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        val gesture = builder.build()
        
        dispatchGesture(gesture, null, null)
    }

    fun changeVolume(up: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

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
