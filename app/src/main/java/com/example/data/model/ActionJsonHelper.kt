package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object ActionJsonHelper {
    fun toJson(actions: List<ShortcutAction>): String {
        val array = JSONArray()
        for (action in actions) {
            val obj = JSONObject().apply {
                put("id", action.id)
                put("type", action.type.name)
                put("title", action.title)
                put("param1", action.param1)
                put("param2", action.param2)
                put("param3", action.param3)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(jsonStr: String?): List<ShortcutAction> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ShortcutAction>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeName = obj.optString("type", ActionType.SPEAK_TEXT.name)
                val type = try {
                    ActionType.valueOf(typeName)
                } catch (e: Exception) {
                    ActionType.SPEAK_TEXT
                }
                list.add(
                    ShortcutAction(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        type = type,
                        title = obj.optString("title", type.displayName),
                        param1 = obj.optString("param1", ""),
                        param2 = obj.optString("param2", ""),
                        param3 = obj.optString("param3", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
