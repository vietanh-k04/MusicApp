package com.example.musicapp.data.local.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Tạo extension property để gọi DataStore (Tên file là settings)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    // Định nghĩa Key (tương tự như Key trong SharedPreferences)
    companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    }

    // 1. Hàm lưu trạng thái (Ghi đè)
    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    // 2. Hàm đọc trạng thái (Dạng Flow - tự động cập nhật khi dữ liệu đổi)
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Mặc định là false (Sáng) nếu chưa lưu gì
            preferences[IS_DARK_MODE_KEY] ?: false
        }
}