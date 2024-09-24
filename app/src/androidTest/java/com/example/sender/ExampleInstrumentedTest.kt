package com.example.sender

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Инструментальный тест, который будет выполняться на устройстве Android.
 *
 * См. документацию по тестированию: http://d.android.com/tools/testing
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Контекст приложения под тестом.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.sender", appContext.packageName)
    }
}
