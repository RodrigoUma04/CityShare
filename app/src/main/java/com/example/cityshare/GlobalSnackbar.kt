package com.example.cityshare


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberSnackbarHostState(): Pair<SnackbarHostState, CoroutineScope> {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return Pair(hostState, scope)
}

@Composable
fun GlobalSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}
