package io.github.acedroidx.danmaku.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.acedroidx.danmaku.model.StartPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    suspend fun getSettings(): SettingsModel {
        return try {
            SettingsModel(
                StartPage.findByStr(
                    context.settingsDataStore.data.first()[SettingsKey.START_PAGE.value]
                        ?: StartPage.HOME.route
                ) ?: StartPage.HOME,
                context.settingsDataStore.data.first()[SettingsKey.CHOSE_PROFILE_ID.value] ?: 1,
                context.settingsDataStore.data.first()[SettingsKey.BILI_COOKIE.value] ?: ""
            )
        } catch (error: NoSuchElementException) {
            SettingsModel(StartPage.HOME, 1, "")
        }
    }

    fun startPage(): Flow<String> =
        context.settingsDataStore.data.map { preferences ->
            preferences[SettingsKey.START_PAGE.value] ?: StartPage.HOME.route
        }

    fun choseProfileId(): Flow<Int> =
        context.settingsDataStore.data.map { preferences ->
            preferences[SettingsKey.CHOSE_PROFILE_ID.value] ?: 1
        }

    fun biliCookie(): Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[SettingsKey.BILI_COOKIE.value] ?: ""
    }

    suspend fun <T> setSettingByKey(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}