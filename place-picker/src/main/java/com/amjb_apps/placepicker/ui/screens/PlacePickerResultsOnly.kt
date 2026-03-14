package com.amjb_apps.placepicker.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.amjb_apps.placepicker.data.PlacePickerConfig
import com.amjb_apps.placepicker.data.SelectedPlace
import com.amjb_apps.placepicker.ui.PlacePickerAction
import com.amjb_apps.placepicker.ui.PlacePickerViewModelFactory
import com.amjb_apps.placepicker.ui.components.EmptyState
import com.amjb_apps.placepicker.ui.components.PredictionsList
import com.amjb_apps.placepicker.ui.state.PlacePickerExposedState

/**
 * Results only - no search field component.
 * Wire your own search input via onStateChange.
 */
@Composable
fun PlacePickerResultsOnly(
    config: PlacePickerConfig,
    onPlaceSelected: (SelectedPlace) -> Unit,
    onStateChange: (PlacePickerExposedState) -> Unit = {},
    modifier: Modifier = Modifier
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

    if (state.predictions.isEmpty() && state.query.isNotEmpty() && !state.isLoading) {
        EmptyState(
            message = "No places found for \"${state.query}\"",
            modifier = modifier.fillMaxSize()
        )
    } else {
        PredictionsList(
            predictions = state.predictions,
            onPredictionClick = { viewModel.onAction(PlacePickerAction.SelectPrediction(it)) },
            modifier = modifier.fillMaxSize()
        )
    }

    ConfirmationDialogIfNeeded(state, config, viewModel)
}
