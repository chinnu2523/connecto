package com.example.connecto

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.connecto", appContext.packageName)
    }

    @Test
    fun testBackendSync() {
        kotlinx.coroutines.runBlocking {
            val sdk = android.os.Build.VERSION.SDK_INT
            android.util.Log.i("ConnectoSyncTest", "=== STARTING SYNC TEST ON ANDROID SDK $sdk ===")
            val health = com.example.connecto.network.ConnectoApiClient.checkHealth()
            android.util.Log.i("ConnectoSyncTest", "checkHealth result: $health")

            val messagesResult = com.example.connecto.network.ConnectoApiClient.getMessages(channelId = "general")
            android.util.Log.i("ConnectoSyncTest", "getMessages result: success=${messagesResult.isSuccess}, count=${messagesResult.getOrNull()?.size}, error=${messagesResult.exceptionOrNull()}")

            val profileResult = com.example.connecto.network.ConnectoApiClient.getProfile("sync_hero")
            android.util.Log.i("ConnectoSyncTest", "getProfile result: success=${profileResult.isSuccess}, profile=${profileResult.getOrNull()}, error=${profileResult.exceptionOrNull()}")
            android.util.Log.i("ConnectoSyncTest", "=== FINISHED SYNC TEST ON ANDROID SDK $sdk ===")
        }
    }
}