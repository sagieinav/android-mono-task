package dev.sagi.monotask.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsRepository(private val context: Context) {

    companion object {
        private val LAST_WORKSPACE_ID = stringPreferencesKey("last_workspace_id")
    }

    suspend fun saveLastWorkspaceId(workspaceId: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_WORKSPACE_ID] = workspaceId
        }
    }

    suspend fun getLastWorkspaceId(): String? {
        return context.dataStore.data.first()[LAST_WORKSPACE_ID]
    }
}
