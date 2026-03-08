package dev.sagi.monotask.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UserSessionViewModel(
    private val userRepository: UserRepository = MonoTaskApp.Companion.instance.userRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    // Stream the whole user doc
    val currentUser: StateFlow<User?> = userRepository
        .getUserStream(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // Display name only
    val displayName: StateFlow<String> = currentUser
        .map { it?.displayName ?: "there" }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ""
        )
}