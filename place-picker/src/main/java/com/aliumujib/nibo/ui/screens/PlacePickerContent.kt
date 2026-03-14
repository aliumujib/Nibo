package com.aliumujib.nibo.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.SelectedPlace
import com.aliumujib.nibo.ui.PlacePickerViewModelFactory
import com.aliumujib.nibo.ui.components.DefaultSearchField
import com.aliumujib.nibo.ui.state.PlacePickerExposedState
import com.aliumujib.nibo.ui.state.SearchFieldState

/**
 * Headless place picker - no top bar.
 * Exposes state via callback for external control.
 */
@Composable
fun PlacePickerContent(
    config: PlacePickerConfig,
    onPlaceSelected: (SelectedPlace) -> Unit,
    onStateChange: (PlacePickerExposedState) -> Unit = {},
    modifier: Modifier = Modifier,
    searchField: @Composable (SearchFieldState) -> Unit = { state ->
        DefaultSearchField(state, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
) {
    val viewModel = remember(config) { PlacePickerViewModelFactory(config).create() }
    val state by viewModel.state.collectAsState()

    HandlePlaceSelection(state, onPlaceSelected)

    val searchFieldState = state.toSearchFieldState(config.searchHint, viewModel)

    LaunchedEffect(searchFieldState, state.predictions, state.isLoading, state.error) {
        onStateChange(
            PlacePickerExposedState(
                searchFieldState = searchFieldState,
                predictions = state.predictions,
                isLoading = state.isLoading,
                error = state.error
            )
        )
    }

    PlacePickerBody(
        searchFieldState = searchFieldState,
        state = state,
        viewModel = viewModel,
        searchField = searchField,
        modifier = modifier
    )

    ConfirmationDialogIfNeeded(state, config, viewModel)
}
