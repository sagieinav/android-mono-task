package dev.sagi.monotask.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


// Single source of truth for the current user's profile. Activity-scoped

@HiltViewModel
class UserSessionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val displayName: StateFlow<String> = currentUser
        .map { it?.displayName ?: "there" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    init {
        viewModelScope.launch {
            val uid = AuthUtils.awaitUid()
            observeUser(uid)
        }
    }

    private suspend fun observeUser(userId: String) {
        userRepository.getUserStream(userId).collect {
            _currentUser.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope is canceled here, stopping the Firestore user stream
        // and the stateIn sharing coroutine for displayName
    }
}
