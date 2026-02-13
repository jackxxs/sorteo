package com.example.sorteo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sorteo_prefs")

class SorteoDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val NOMBRES_KEY = stringSetPreferencesKey("nombres_inscritos")
    }

    val nombres: Flow<List<String>> = dataStore.data.map {
        it[NOMBRES_KEY]?.toList() ?: emptyList()
    }

    suspend fun guardarNombres(nombres: List<String>) {
        dataStore.edit {
            it[NOMBRES_KEY] = nombres.toSet()
        }
    }
}