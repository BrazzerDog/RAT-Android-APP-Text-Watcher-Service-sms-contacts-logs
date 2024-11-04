package com.example.sender

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RealtimeDataSenderService : Service() {

    private val client = OkHttpClient()
    private val serverUrl = "http://your server ip here:port/api/text"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra("text")
        if (text != null) {
            sendDataToServer(text)
        }
        return START_NOT_STICKY
    }

    private fun sendDataToServer(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPreferences = applicationContext.getSharedPreferences("AppPrefs", MODE_PRIVATE)
                val serialNumber = sharedPreferences.getString("serial_number", null)

                if (serialNumber == null) {
                    Log.e("RealtimeDataSenderService", "Serial number not found")
                    return@launch
                }

                val jsonObject = JSONObject()
                jsonObject.put("deviceSerial", serialNumber)
                jsonObject.put("inputText", text)
                val json = jsonObject.toString()

                Log.d("RealtimeDataSenderService", "Sending JSON to server: $json")

                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(serverUrl)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    Log.d("RealtimeDataSenderService", "Data sent successfully")
                }
            } catch (e: Exception) {
                Log.e("RealtimeDataSenderService", "Error sending data", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}