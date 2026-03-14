package com.amjb_apps.placepicker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amjb_apps.placepicker.data.PlacePickerConfig
import com.amjb_apps.placepicker.data.SelectedPlace
import com.amjb_apps.placepicker.ui.PlacePickerViewModelFactory
import com.amjb_apps.placepicker.ui.components.DefaultSearchField
import com.amjb_apps.placepicker.ui.components.DefaultTopBar
import com.amjb_apps.placepicker.ui.state.SearchFieldState
import com.amjb_apps.placepicker.ui.state.TopBarState

/**
 * Place picker without Scaffold wrapper.
 * Use when embedding in your own layout.
 */
@Composable
fun PlacePickerLayout(
    config: PlacePickerConfig,
    onPlaceSelected: (SelectedPlace) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    topBar: (@Composable (TopBarState) -> Unit)? = { DefaultTopBar(it) },
    searchField: @Composable (SearchFieldState) -> Unit = { state ->
        DefaultSearchField(state, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
) {
    val viewModel = remember(config) { PlacePickerViewModelFactory(config).create() }
    val state by viewModel.state.collectAsState()

    val topBarState = remember(config.title, onDismiss) {
        TopBarState(title = config.title, onBackClick = onDismiss)
    }

    HandlePlaceSelection(state, onPlaceSelected)

    val searchFieldState = state.toSearchFieldState(config.searchHint, viewModel)

    Column(modifier = modifier) {
        topBar?.invoke(topBarState)

        PlacePickerBody(
            searchFieldState = searchFieldState,
            state = state,
            viewModel = viewModel,
            searchField = searchField,
            modifier = Modifier.fillMaxSize()
        )
    }

    ConfirmationDialogIfNeeded(state, config, viewModel)
}
