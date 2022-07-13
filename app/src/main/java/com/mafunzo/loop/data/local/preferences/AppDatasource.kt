package com.mafunzo.loop.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mafunzo_data")

class AppDatasource @Inject constructor(@ApplicationContext context: Context) {
    private val applicationContext = context.applicationContext

    companion object {
        val CURRENT_WORKSPACE = stringPreferencesKey("current_workspace")
        val ACCOUNT_TYPE = stringPreferencesKey("account_type")
    }

    suspend fun saveCurrentWorkspace(workspace: String) {
        applicationContext.dataStore.edit { preferences ->
            preferences[CURRENT_WORKSPACE] = workspace
        }
    }

    fun getCurrentWorkSpace(): Flow<String?> = applicationContext.dataStore.data.map { preferences ->
        preferences[CURRENT_WORKSPACE]
    }

    suspend fun clear() {
        applicationContext.dataStore.edit {
            it.clear()
        }
    }

    suspend fun saveAccountType(accountType: String) {
        applicationContext.dataStore.edit { preferences ->
            preferences[ACCOUNT_TYPE] = accountType
        }
    }

    fun getAccountType(): Flow<String?> = applicationContext.dataStore.data.map { preferences ->
        preferences[ACCOUNT_TYPE]
    }
}