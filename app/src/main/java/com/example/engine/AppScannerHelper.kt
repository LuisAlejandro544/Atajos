package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppScannerHelper {

    /**
     * Escanea todas las aplicaciones y juegos instalados en el sistema en un hilo secundario (IO).
     * Extrae el nombre, paquete, categoría de juego y el icono convertido a ImageBitmap.
     */
    suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val myPackageName = context.packageName
        val appsList = mutableListOf<AppInfo>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == myPackageName || seenPackages.contains(packageName)) {
                continue
            }
            seenPackages.add(packageName)

            val appName = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                packageName
            }

            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(packageName, 0)
                }
            } catch (e: Exception) {
                null
            }

            val isGame = checkIfGame(appInfo, packageName, appName)
            val iconBitmap = try {
                val drawable = resolveInfo.loadIcon(packageManager)
                drawableToBitmap(drawable)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }

            appsList.add(
                AppInfo(
                    name = appName,
                    packageName = packageName,
                    isGame = isGame,
                    iconBitmap = iconBitmap
                )
            )
        }

        // Ordenar alfabéticamente por nombre
        appsList.sortedWith(compareByDescending<AppInfo> { it.isGame }.thenBy { it.name.lowercase() })
    }

    private fun checkIfGame(appInfo: ApplicationInfo?, packageName: String, appName: String): Boolean {
        if (appInfo != null) {
            // Android 8.0+ Category Game
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    return true
                }
            }

            // Flag is game (deprecated en O, pero válido para versiones anteriores)
            @Suppress("DEPRECATION")
            if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                return true
            }
        }

        // Heurística complementaria por palabras clave comunes en juegos
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()
        val gameKeywords = listOf("game", "juego", "unity", "minecraft", "roblox", "clash", "candy", "pubg", "freefire", "asphalt", "craft", "subway", "angry", "runner", "simulator", "puzzle", "rpg", "arcade")
        return gameKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(144) else 72
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(144) else 72

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
