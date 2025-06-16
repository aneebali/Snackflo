//package com.salesflo.snackflo.repository
//
//import android.content.Context
//
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.stringPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//import kotlinx.coroutines.flow.first
//
//
//private val Context.dataStore by preferencesDataStore(name = "settings")
//
//class AndroidSettingsDataStore(private val context: Context) : SettingsDataStore {
//    override suspend fun putString(key: String, value: String) {
//        val keyPref = stringPreferencesKey(key)
//        context.dataStore.edit { it[keyPref] = value }
//    }
//
//    override suspend fun getString(key: String): String? {
//        val keyPref = stringPreferencesKey(key)
//        return context.dataStore.data.first()[keyPref]
//    }
//}
//
//lateinit var appContext: Context
//
//fun provideSettingsDataStore(): SettingsDataStore {
//    return AndroidSettingsDataStore(appContext)
//}
//
