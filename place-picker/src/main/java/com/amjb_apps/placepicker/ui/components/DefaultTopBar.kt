package com.amjb_apps.placepicker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amjb_apps.placepicker.ui.state.TopBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    state: TopBarState,
    modifier: Modifier = Modifier
) {
    LargeTopAppBar(
        title = { Text(state.title) },
        navigationIcon = {
            IconButton(onClick = state.onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        modifier = modifier
    )
}
