package com.example.data.repository

import android.content.Context
import android.content.Intent
import com.example.data.model.InstalledAppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    suspend fun getInstalledLauncherApps(): List<InstalledAppItem> {
        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val launcherApps = pm.queryIntentActivities(mainIntent, 0)
                launcherApps.mapNotNull { resolveInfo ->
                    try {
                        val pkg = resolveInfo.activityInfo.packageName
                        val label = resolveInfo.loadLabel(pm).toString().trim()
                        if (label.isNotBlank()) InstalledAppItem(name = label, packageName = pkg) else null
                    } catch (_: Exception) {
                        null
                    }
                }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
