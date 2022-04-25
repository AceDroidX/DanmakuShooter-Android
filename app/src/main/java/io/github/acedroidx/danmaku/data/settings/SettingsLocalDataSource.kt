package io.github.acedroidx.danmaku.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    suspend fun getSettings(): SettingsModel {
        val BILI_COOKIE = stringPreferencesKey("bili_cookie")
        try {
            return SettingsModel(context.dataStore.data.first()[BILI_COOKIE] ?: "")
        } catch (error: NoSuchElementException) {
            return SettingsModel("")
        }
    }

    suspend fun setSettings(settings: SettingsModel) {
        val BILI_COOKIE = stringPreferencesKey("bili_cookie")
        context.dataStore.edit { preferences ->
            preferences[BILI_COOKIE] = settings.biliCookie
        }
    }
}