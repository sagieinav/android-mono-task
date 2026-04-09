package dev.sagi.monotask.domain.fake

import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    private val _user = MutableStateFlow<User?>(null)

    var user: User?
        get() = _user.value
        set(value) { _user.value = value }

    override fun getUserStream(userId: String): Flow<User?> = _user

    override suspend fun getUserOnce(userId: String): User? = _user.value

    override suspend fun getUserById(uid: String): User? = _user.value

    override suspend fun createUserIfNotExists(user: User) {
        if (_user.value == null) _user.value = user
    }

    override suspend fun updateProfile(userId: String, displayName: String) {
        _user.value = _user.value?.copy(displayName = displayName)
    }

    override suspend fun updateAvatarPreset(userId: String, preset: Int) {
        _user.value = _user.value?.copy(avatarPreset = preset)
    }

    override suspend fun completeOnboarding(userId: String) {
        _user.value = _user.value?.copy(onboarded = true)
    }

    override suspend fun updateHyperfocusMode(userId: String, enabled: Boolean) {
        _user.value = _user.value?.copy(hyperfocusModeEnabled = enabled)
    }

    override suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float) {
        _user.value = _user.value?.copy(dueDateWeight = dueDateWeight)
    }

    override suspend fun addFriend(userId: String, friendId: String) {}

    override suspend fun addFriendBatch(userId: String, friendId: String) {}

    override suspend fun removeFriendBatch(userId: String, friendId: String) {}

    override suspend fun searchUsers(query: String): List<User> = emptyList()
}
