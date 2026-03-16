package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants

// ====================
// Tab 3 — Social: friend search + friends list
// ====================

@Composable
fun SocialTab(
    friends: List<String>,
    searchResults: List<User>,
    isSearching: Boolean,
    onProfileEvent: (ProfileEvent) -> Unit,
    bottomPadding: Dp
) {
    var query by remember { mutableStateOf("") }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            top = Constants.Theme.SCREEN_PADDING,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search field
        item {
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it; onProfileEvent(ProfileEvent.SearchUsers(it)) },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Search by username…") },
                leadingIcon   = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp)
            )
        }

        // Search loading indicator
        if (isSearching) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        // Search results
        if (searchResults.isNotEmpty()) {
            item {
                Text(
                    text       = "Results",
                    style      = MaterialTheme.typography.labelLarge,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            items(searchResults, key = { it.id }) { user ->
                UserSearchRow(
                    user      = user,
                    isAlready = user.id in friends,
                    onAdd     = { onProfileEvent(ProfileEvent.AddFriend(user.id)); query = "" }
                )
            }
        }

        // Friends list header
        item {
            Text(
                text       = "Friends (${friends.size})",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Empty friends state
        if (friends.isEmpty()) {
            item {
                Text(
                    text      = "Search for users above to add your first friend.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }

        // Friend rows. IDs only for now, upgrade when a friends sub-collection lands
        items(friends, key = { it }) { friendId ->
            FriendIdRow(friendId = friendId)
        }
    }
}

// ====================
// Row composables
// ====================

@Composable
private fun UserSearchRow(
    user: User,
    isAlready: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(name = user.displayName, sizeDp = 40)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text       = user.displayName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = "Level ${user.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isAlready) {
            IconButton(onClick = onAdd) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_person),
                    contentDescription = "Add friend",
                    tint             = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Text(
                text       = "✓",
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FriendIdRow(friendId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarCircle(name = "?", sizeDp = 40)
        Spacer(Modifier.width(12.dp))
        Text(
            // Truncated ID — will become a real name when friend data is fetched
            text  = friendId.take(16) + if (friendId.length > 16) "…" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ====================
// Avatar circle — first letter fallback
// ====================

@Composable
fun AvatarCircle(name: String, sizeDp: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = name.firstOrNull()?.uppercase() ?: "?",
            color      = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize   = (sizeDp / 2.5).sp
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SocialTabPreview() {
    MonoTaskTheme {
        SocialTab(
            friends        = listOf("uid_abc123", "uid_def456"),
            searchResults  = listOf(
                User(id = "2", displayName = "Roei Zalah",  level = 12, xp = 3400),
                User(id = "3", displayName = "Ofek Fanian",  level = 7,  xp = 1200)
            ),
            isSearching    = false,
            onProfileEvent = {},
            bottomPadding  = 0.dp
        )
    }
}