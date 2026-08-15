package com.example.data.db

import com.example.data.defaults.DefaultAutomationsList
import com.example.data.defaults.DefaultShortcutsList
import com.example.data.defaults.GalleryTemplatesList
import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutEntity

/**
 * Fachada centralizada y modular para obtener atajos predeterminados,
 * plantillas de la galería y automatizaciones iniciales.
 */
object DefaultShortcutsProvider {

    fun getDefaultShortcuts(): List<ShortcutEntity> = DefaultShortcutsList.get()

    fun getGalleryTemplates(): List<ShortcutEntity> = GalleryTemplatesList.get()

    fun getDefaultAutomations(): List<AutomationEntity> = DefaultAutomationsList.get()
}

