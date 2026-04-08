package dev.sagi.monotask.data.repository

import android.util.Log
import androidx.annotation.DrawableRes
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    private fun userDoc(userId: String) =
        db.collection("users")
            .document(userId)

    override fun getUserStream(userId: String): Flow<User?> =
        userDoc(userId)
            .snapshots()
            .map {
                it.toObject(User::class.java)?.copy(id = it.id)?.migrateAvatar()
                    ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${it.id}"); null }
            }

    override suspend fun getUserOnce(userId: String): User? {
        val doc = userDoc(userId)
            .get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)?.migrateAvatar()
            ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${doc.id}"); null }
    }

    /** One-time migration: old preset=0 (DiceBear auto) → preset=1. */
    private suspend fun User.migrateAvatar(): User {
        if (avatarPreset != 0) return this
        updateAvatarPreset(id, 1)
        return copy(avatarPreset = 1)
    }

    override suspend fun getUserById(uid: String): User? = getUserOnce(uid)

    override suspend fun createUserIfNotExists(user: User) {
        val doc = userDoc(user.id)
            .get().await()
        if (!doc.exists())
            userDoc(user.id)
                .set(user).await()
    }

    override suspend fun updateProfile(userId: String, displayName: String) {
        userDoc(userId)
            .update("displayName", displayName)
            .await()
    }

    override suspend fun updateAvatarPreset(userId: String, @DrawableRes preset: Int) {
        userDoc(userId)
            .update("avatarPreset", preset)
            .await()
    }

    override suspend fun completeOnboarding(userId: String) {
        userDoc(userId)
            .update("onboarded", true)
            .await()
    }

    override suspend fun updateHyperfocusMode(userId: String, enabled: Boolean) {
        userDoc(userId)
            .update("hyperfocusModeEnabled", enabled)
            .await()
    }

    override suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float) {
        userDoc(userId)
            .update("dueDateWeight", dueDateWeight)
            .await()
    }

    override suspend fun addFriend(userId: String, friendId: String) {
        userDoc(userId)
            .update("friends", FieldValue.arrayUnion(friendId))
            .await()
    }

    override suspend fun addFriendBatch(userId: String, friendId: String) {
        db.batch().apply {
            update(userDoc(userId),   "friends", FieldValue.arrayUnion(friendId))
            update(userDoc(friendId), "friends", FieldValue.arrayUnion(userId))
        }
            .commit().await()
    }

    override suspend fun removeFriendBatch(userId: String, friendId: String) {
        db.batch().apply {
            update(userDoc(userId),   "friends", FieldValue.arrayRemove(friendId))
            update(userDoc(friendId), "friends", FieldValue.arrayRemove(userId))
        }
            .commit().await()
    }

    override suspend fun searchUsers(query: String): List<User> {
        val result = db.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uF8FF")
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(User::class.java)?.copy(id = it.id)
                ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${it.id}"); null }
        }
    }
}
