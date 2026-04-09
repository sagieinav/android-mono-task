package dev.sagi.monotask.data.demo

import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class DemoUserRepository @Inject constructor() : UserRepository {

    private val _users = mapOf(
        DemoSeedData.DEMO_USER_ID to MutableStateFlow<User?>(DemoSeedData.DEMO_USER),
        DemoSeedData.DEMO_FRIEND_ROEI_ID to MutableStateFlow<User?>(DemoSeedData.DEMO_FRIEND_ROEI),
        DemoSeedData.DEMO_FRIEND_OFEK_ID to MutableStateFlow<User?>(DemoSeedData.DEMO_FRIEND_OFEK),
    )
    private val _user get() = _users[DemoSeedData.DEMO_USER_ID]!!

    override fun getUserStream(userId: String): Flow<User?> =
        _users[userId] ?: flowOf(null)

    override suspend fun getUserOnce(userId: String): User? = _users[userId]?.value

    override suspend fun getUserById(uid: String): User? = _users[uid]?.value

    override suspend fun createUserIfNotExists(user: User) = Unit

    override suspend fun updateProfile(userId: String, displayName: String) {
        _user.update { it?.copy(displayName = displayName) }
    }

    override suspend fun updateAvatarPreset(userId: String, preset: Int) {
        _user.update { it?.copy(avatarPreset = preset) }
    }

    override suspend fun completeOnboarding(userId: String) {
        _user.update { it?.copy(onboarded = true) }
    }

    override suspend fun updateHyperfocusMode(userId: String, enabled: Boolean) {
        _user.update { it?.copy(hyperfocusModeEnabled = enabled) }
    }

    override suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float) {
        _user.update { it?.copy(dueDateWeight = dueDateWeight) }
    }

    override suspend fun addFriend(userId: String, friendId: String) = Unit

    override suspend fun addFriendBatch(userId: String, friendId: String) = Unit

    override suspend fun removeFriendBatch(userId: String, friendId: String) = Unit

    override suspend fun searchUsers(query: String): List<User> = emptyList()
}
