package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier.size(86.dp),
    useContained: Boolean = false // container UI or not
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (useContained) {
            ContainedLoadingIndicator(
                indicatorColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = modifier
            )
        } else {
            LoadingIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingSpinnerPreview() {
    MonoTaskTheme {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            LoadingSpinner()                    // default size
//            LoadingSpinner(size = 48.dp)        // small
            LoadingSpinner(Modifier.size(86.dp), true)        // large
        }
    }
}
