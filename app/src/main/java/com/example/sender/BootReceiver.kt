package com.example.sender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, starting services")
            context.startService(Intent(context, TextWatcherService::class.java))
            // Можете также запустить WorkManager здесь, если необходимо
        }
    }
}