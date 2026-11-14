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
        var direction = AudioManager.ADJUST_LOWER
        
        if (up) {
            direction = AudioManager.ADJUST_RAISE
        }
        
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC, 
            direction, 
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun changeBrightness(up: Boolean) {
        try {
            val current = Settings.System.getInt(
                contentResolver, 
                Settings.System.SCREEN_BRIGHTNESS
            )
            
            var target = current - 30
            if (up) {
                target = current + 30
            }
            
            if (target > 255) target = 255
            if (target < 10) target = 10
            
            Settings.System.putInt(
                contentResolver, 
                Settings.System.SCREEN_BRIGHTNESS, 
                target
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
