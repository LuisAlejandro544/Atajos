package com.example

import android.app.Application
import android.util.Log
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ShortcutsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initNetworkClient()
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
