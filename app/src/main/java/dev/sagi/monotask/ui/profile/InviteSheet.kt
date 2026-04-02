package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.display.AvatarBox
import dev.sagi.monotask.ui.component.core.MonoBottomSheet
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==========================================================================
// State
// ==========================================================================

sealed class InviteSenderState {
    object Loading                            : InviteSenderState()
    object NotFound                           : InviteSenderState()
    object SelfInvite                         : InviteSenderState()
    data class AlreadyFriends(val user: User) : InviteSenderState()
    data class Ready(val user: User)          : InviteSenderState()
}

// ==========================================================================
// ViewModel
// ==========================================================================

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _senderState = MutableStateFlow<InviteSenderState>(InviteSenderState.Loading)
    val senderState: StateFlow<InviteSenderState> = _senderState.asStateFlow()

    private var currentUserId = ""

    fun load(senderUid: String) {
        viewModelScope.launch {
            currentUserId = AuthUtils.awaitUid()
            val sender = userRepository.getUserById(senderUid)
            _senderState.value = when {
                sender == null                        -> InviteSenderState.NotFound
                senderUid == currentUserId            -> InviteSenderState.SelfInvite
                currentUserId in sender.friends       -> InviteSenderState.AlreadyFriends(sender)
                else                                  -> InviteSenderState.Ready(sender)
            }
        }
    }

    fun accept(senderUid: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.addFriendBatch(currentUserId, senderUid)
            } finally {
                onDone()
            }
        }
    }
}

// ==========================================================================
// Composable
// ==========================================================================

@Composable
fun InviteSheet(
    senderUid: String,
    onDismiss: () -> Unit,
    vm: InviteViewModel = hiltViewModel()
) {
    LaunchedEffect(senderUid) { vm.load(senderUid) }

    val state by vm.senderState.collectAsStateWithLifecycle()

    MonoBottomSheet(
        title            = "Friend Request",
        onDismissRequest = onDismiss
    ) {
        when (val s = state) {
            is InviteSenderState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is InviteSenderState.NotFound -> {
                InfoMessage("User not found.")
                Spacer(Modifier.height(8.dp))
                ActionButton(onClick = onDismiss) {
                    Text("Dismiss", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            is InviteSenderState.SelfInvite -> {
                InfoMessage("You can't add yourself.")
                Spacer(Modifier.height(8.dp))
                ActionButton(onClick = onDismiss) {
                    Text("Dismiss", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            is InviteSenderState.AlreadyFriends -> {
                SenderProfile(user = s.user)
                Spacer(Modifier.height(4.dp))
                InfoMessage("You're already friends with ${s.user.displayName}.")
                Spacer(Modifier.height(8.dp))
                ActionButton(onClick = onDismiss) {
                    Text("Dismiss", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            is InviteSenderState.Ready -> {
                SenderProfile(user = s.user)
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = "${s.user.displayName} wants to be your friend on MonoTask",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        color    = MaterialTheme.colorScheme.outline
                    ) {
                        Text("Decline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    ActionButton(
                        onClick  = { vm.accept(senderUid, onDismiss) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ==========================================================================
// Private helpers
// ==========================================================================

@Composable
private fun SenderProfile(user: User) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AvatarBox(
            user     = user,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text       = user.displayName,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text  = "Lv. ${user.level}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun InfoMessage(text: String) {
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodyMedium,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier  = Modifier.fillMaxWidth()
    )
}
