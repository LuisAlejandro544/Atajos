package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ShortcutEntity

/**
 * Gestor dinámico de accesos directos en el launcher del dispositivo (App Shortcuts al mantener presionado el icono).
 */
object AppShortcutsHelper {

    fun updateDynamicShortcuts(context: Context, shortcuts: List<ShortcutEntity>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        try {
            // Tomamos los atajos favoritos o los más ejecutados (máximo 4 para el launcher)
            val topShortcuts = shortcuts
                .sortedWith(compareByDescending<ShortcutEntity> { it.isFavorite }.thenByDescending { it.runCount })
                .take(4)

            val shortcutInfoList = topShortcuts.mapIndexed { index, shortcut ->
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.ACTION_RUN_SHORTCUT"
                    putExtra("EXTRA_SHORTCUT_ID", shortcut.id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val iconRes = when (shortcut.iconKey) {
                    "flashlight_on" -> android.R.drawable.ic_dialog_alert
                    "sports_esports" -> android.R.drawable.ic_media_play
                    "chat" -> android.R.drawable.ic_menu_send
                    "record_voice_over" -> android.R.drawable.ic_btn_speak_now
                    "search" -> android.R.drawable.ic_menu_search
                    else -> android.R.drawable.ic_menu_agenda
                }

                ShortcutInfoCompat.Builder(context, "shortcut_${shortcut.id}")
                    .setShortLabel(shortcut.title.take(15))
                    .setLongLabel(shortcut.title)
                    .setIcon(IconCompat.createWithResource(context, iconRes))
                    .setIntent(intent)
                    .setRank(index)
                    .build()
            }

            ShortcutManagerCompat.setDynamicShortcuts(context, shortcutInfoList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
