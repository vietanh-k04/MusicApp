package com.example.musicapp.ui.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.source.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val isDarkTheme = dataStoreManager.isDarkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveTheme(isDark)
        }
    }

    fun setLanguage(code: String) {
        val localeList = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}