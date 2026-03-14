package com.amjb_apps.placepicker.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amjb_apps.placepicker.data.PlacePickerConfig
import com.amjb_apps.placepicker.data.SelectedPlace
import com.amjb_apps.placepicker.ui.PlacePickerAction
import com.amjb_apps.placepicker.ui.PlacePickerViewModelFactory
import com.amjb_apps.placepicker.ui.components.DefaultSearchField
import com.amjb_apps.placepicker.ui.components.DefaultTopBar
import com.amjb_apps.placepicker.ui.state.SearchFieldState
import com.amjb_apps.placepicker.ui.state.TopBarState

/**
 * Full-screen place picker with Scaffold, top bar, and snackbar.
 */
@Composable
fun PlacePickerScreen(
    config: PlacePickerConfig,
    onPlaceSelected: (SelectedPlace) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable (TopBarState) -> Unit = { DefaultTopBar(it) },
    searchField: @Composable (SearchFieldState) -> Unit = { state ->
        DefaultSearchField(state, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
) {
    val viewModel = remember(config) { PlacePickerViewModelFactory(config).create() }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val topBarState = remember(config.title, onDismiss) {
        TopBarState(title = config.title, onBackClick = onDismiss)
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onAction(PlacePickerAction.ClearError)
        }
    }

    HandlePlaceSelection(state, onPlaceSelected)

    val searchFieldState = state.toSearchFieldState(config.searchHint, viewModel)

    Scaffold(
        topBar = { topBar(topBarState) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        PlacePickerBody(
            searchFieldState = searchFieldState,
            state = state,
            viewModel = viewModel,
            searchField = searchField,
            modifier = Modifier.padding(innerPadding)
        )
    }

    ConfirmationDialogIfNeeded(state, config, viewModel)
}
