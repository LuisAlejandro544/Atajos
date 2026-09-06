package com.example

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.trigger.PowerTriggerReceiver
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ShortcutsApp : Application() {

    private val powerTriggerReceiver = PowerTriggerReceiver()

    override fun onCreate() {
        super.onCreate()
        initNetworkClient()
        initAnrWatchDog()
        initPowerTriggerReceiver()
    }

    private fun initPowerTriggerReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(powerTriggerReceiver, filter)
            Log.d("ShortcutsApp", "PowerTriggerReceiver registrado dinámicamente.")
        } catch (e: Exception) {
            Log.w("ShortcutsApp", "No se pudo registrar dinámicamente PowerTriggerReceiver: ${e.message}")
        }
    }

    private fun initAnrWatchDog() {
        try {
            // ANR-WatchDog vigila el hilo de interfaz (UI Thread) y registra trazas completas si hay bloqueos
            val watchdogClass = Class.forName("com.github.anrwatchdog.ANRWatchDog")
            val constructor = watchdogClass.getConstructor(Int::class.javaPrimitiveType)
            val watchdogInstance = constructor.newInstance(4000) // 4 segundos de umbral antes de ANR
            val startMethod = watchdogClass.getMethod("start")
            startMethod.invoke(watchdogInstance)
            Log.d("ShortcutsApp", "ANR-WatchDog inicializado exitosamente (umbral: 4000ms).")
        } catch (_: ClassNotFoundException) {
            // Ausente en variantes release si no estuviera incluido
        } catch (e: Throwable) {
            Log.w("ShortcutsApp", "No se pudo iniciar ANR-WatchDog: ${e.message}")
        }
    }

    private fun initNetworkClient() {
        try {
            // Inicialización de OkHttpClient integrado con ChuckerInterceptor para inspección de red
            val chuckerInterceptor = com.chuckerteam.chucker.api.ChuckerInterceptor.Builder(this)
                .collector(com.chuckerteam.chucker.api.ChuckerCollector(this))
                .maxContentLength(250_000L)
                .alwaysReadResponseBody(true)
                .build()

            val client = OkHttpClient.Builder()
                .addInterceptor(chuckerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            NetworkClientProvider.okHttpClient = client
            Log.d("ShortcutsApp", "Chucker OkHttpClient inicializado con éxito.")
        } catch (e: Throwable) {
            Log.w("ShortcutsApp", "No se pudo inicializar ChuckerInterceptor: ${e.message}")
        }
    }
}

object NetworkClientProvider {
    var okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
}
