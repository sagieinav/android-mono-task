package dev.sagi.monotask

import BottomNavBarElevated
import BottomNavBarFlat
import ConfirmDialog
import FloatingNavBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.sagi.monotask.ui.theme.MonoTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTaskTheme {
                val hazeState = remember { HazeState() }
                var selected by remember { mutableStateOf(NavTab.FOCUS) }
                var showDialog by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .haze(hazeState)
                ) {
                    // Busy background so blur is visible
                    Text(
                        text = "Background\ncontent\nhere",
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(top = 120.dp),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "More stuff\nin the middle",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displaySmall
                    )

                    // Trigger dialog
                    androidx.compose.material3.Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(top = 60.dp)
                    ) {
                        Text("Show Dialog")
                    }

                    FloatingNavBar(
                        selectedTab = selected,
                        onTabSelected = { selected = it },
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    if (showDialog) {
                        ConfirmDialog(
                            title = "Delete Task?",
                            message = "This cannot be undone.",
                            confirmLabel = "Delete",
                            isDestructive = true,
                            onConfirm = { showDialog = false },
                            onDismiss = { showDialog = false }
                        )
                    }
                }
            }
        }
    }
}
