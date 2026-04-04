package dev.sagi.monotask.domain.repository

import androidx.annotation.DrawableRes
import dev.sagi.monotask.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStream(userId: String): Flow<User?>
    suspend fun getUserOnce(userId: String): User?
    suspend fun getUserById(uid: String): User?
    suspend fun createUserIfNotExists(user: User)
    suspend fun updateProfile(userId: String, displayName: String)
    suspend fun updateAvatarPreset(userId: String, @DrawableRes preset: Int)
    suspend fun completeOnboarding(userId: String)
    suspend fun updateHyperfocusMode(userId: String, enabled: Boolean)
    suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float)
    suspend fun addFriend(userId: String, friendId: String)
    suspend fun addFriendBatch(userId: String, friendId: String)
    suspend fun removeFriendBatch(userId: String, friendId: String)
    suspend fun searchUsers(query: String): List<User>
}
