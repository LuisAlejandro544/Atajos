package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActionBlock
import com.example.data.model.ActionBlockConverter
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ShortcutEntity::class], version = 4, exportSchema = false)
@TypeConverters(ActionBlockConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atajos_database_v4"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.shortcutDao())
                }
            }
        }

        suspend fun populateInitialData(dao: ShortcutDao) {
            val defaults = listOf(
                ShortcutEntity(
                    title = "Linterna Rápida",
                    description = "Enciende o apaga el flash",
                    colorHex = "#FF9500",
                    iconKey = "FLASH",
                    actionType = ActionType.FLASHLIGHT.name,
                    parameter = "",
                    isFavorite = true,
                    category = "Utilidades"
                ),
                ShortcutEntity(
                    title = "Ruta a Casa",
                    description = "Navegación directa en Maps",
                    colorHex = "#007AFF",
                    iconKey = "MAP",
                    actionType = ActionType.MAP_NAV.name,
                    parameter = "Casa",
                    isFavorite = true,
                    category = "Viajes"
                ),
                ShortcutEntity(
                    title = "Temporizador 5m",
                    description = "Inicia cuenta regresiva",
                    colorHex = "#FF2D55",
                    iconKey = "TIMER",
                    actionType = ActionType.SET_TIMER.name,
                    parameter = "5",
                    isFavorite = true,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Copiar Mi Correo",
                    description = "contacto@ejemplo.com",
                    colorHex = "#34C759",
                    iconKey = "COPY",
                    actionType = ActionType.COPY_TEXT.name,
                    parameter = "contacto@ejemplo.com",
                    isFavorite = false,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Buscar en Web",
                    description = "Abre Google en navegador",
                    colorHex = "#5856D6",
                    iconKey = "WEB",
                    actionType = ActionType.OPEN_URL.name,
                    parameter = "https://www.google.com",
                    isFavorite = false,
                    category = "Navegación"
                ),
                ShortcutEntity(
                    title = "Modo de Sonido",
                    description = "Abre ajustes de volumen",
                    colorHex = "#AF52DE",
                    iconKey = "SOUND",
                    actionType = ActionType.SOUND_SETTINGS.name,
                    parameter = "",
                    isFavorite = false,
                    category = "Ajustes"
                ),
                ShortcutEntity(
                    title = "Mensaje Rápido",
                    description = "«¡Llego en 5 minutos!»",
                    colorHex = "#00C7BE",
                    iconKey = "MESSAGE",
                    actionType = ActionType.SEND_MESSAGE.name,
                    parameter = "¡Llego en 5 minutos!",
                    isFavorite = false,
                    category = "Comunicación"
                ),
                ShortcutEntity(
                    title = "Compartir Nota",
                    description = "Comparte un texto rápido",
                    colorHex = "#FF3B30",
                    iconKey = "SHARE",
                    actionType = ActionType.SHARE_TEXT.name,
                    parameter = "¡Hola! Te comparto esta información importante.",
                    isFavorite = false,
                    category = "Comunicación"
                ),
                ShortcutEntity(
                    title = "Script Inteligente Lua",
                    description = "Lógica y condición horaria",
                    colorHex = "#5856D6",
                    iconKey = "CODE",
                    actionType = ActionType.LUA_SCRIPT.name,
                    actions = listOf(
                        ActionBlock(
                            actionType = ActionType.LUA_SCRIPT.name,
                            parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
                            customLabel = "Ejecutar lógica condicional Lua"
                        )
                    ),
                    parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
                    isFavorite = true,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Aviso de Voz",
                    description = "Lee un mensaje con la voz del sistema",
                    colorHex = "#FF2D55",
                    iconKey = "SPEAK",
                    actionType = ActionType.SPEAK.name,
                    parameter = "Atajo ejecutado correctamente",
                    isFavorite = true,
                    category = "Utilidades"
                )
            )
            dao.insertAll(defaults)
        }
    }
}
