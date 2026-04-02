package dev.sagi.monotask.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sagi.monotask.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPrefsRepository {

    companion object {
        private val LAST_WORKSPACE_ID = stringPreferencesKey("last_workspace_id")
    }

    override suspend fun saveLastWorkspaceId(workspaceId: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_WORKSPACE_ID] = workspaceId
        }
    }

    override suspend fun getLastWorkspaceId(): String? {
        return context.dataStore.data.first()[LAST_WORKSPACE_ID]
    }
}
