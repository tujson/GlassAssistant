package dev.synople.glassassistant.utils

import androidx.datastore.preferences.core.stringPreferencesKey

object GlassAssistantConstants {
    val DATASTORE_OPEN_AI_API_KEY =
        stringPreferencesKey("openAiApiKey")
}