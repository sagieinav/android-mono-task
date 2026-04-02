package dev.sagi.monotask.domain.repository

interface UserPrefsRepository {
    suspend fun saveLastWorkspaceId(workspaceId: String)
    suspend fun getLastWorkspaceId(): String?
}
