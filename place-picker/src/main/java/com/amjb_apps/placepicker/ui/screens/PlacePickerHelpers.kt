package com.amjb_apps.placepicker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amjb_apps.placepicker.data.PlacePickerConfig
import com.amjb_apps.placepicker.data.SelectedPlace
import com.amjb_apps.placepicker.ui.PlacePickerAction
import com.amjb_apps.placepicker.ui.PlacePickerState
import com.amjb_apps.placepicker.ui.PlacePickerViewModel
import com.amjb_apps.placepicker.ui.components.EmptyState
import com.amjb_apps.placepicker.ui.components.PlaceConfirmationDialog
import com.amjb_apps.placepicker.ui.components.PredictionsList
import com.amjb_apps.placepicker.ui.state.SearchFieldState

@Composable
internal fun HandlePlaceSelection(
    state: PlacePickerState,
    onPlaceSelected: (SelectedPlace) -> Unit
) {
    LaunchedEffect(state.selectedPlace, state.showConfirmationDialog) {
        if (state.selectedPlace != null && !state.showConfirmationDialog) {
            onPlaceSelected(state.selectedPlace)
        }
    }
}

internal fun PlacePickerState.toSearchFieldState(
    hint: String,
    viewModel: PlacePickerViewModel
) = SearchFieldState(
    query = query,
    hint = hint,
    isLoading = isLoading,
    onQueryChange = { viewModel.onAction(PlacePickerAction.UpdateQuery(it)) },
    onClear = { viewModel.onAction(PlacePickerAction.ClearQuery) }
)

@Composable
internal fun PlacePickerBody(
    searchFieldState: SearchFieldState,
    state: PlacePickerState,
    viewModel: PlacePickerViewModel,
    searchField: @Composable (SearchFieldState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        searchField(searchFieldState)
        Spacer(Modifier.height(8.dp))
        
        if (state.predictions.isEmpty() && state.query.isNotEmpty() && !state.isLoading) {
            EmptyState(
                message = "No places found for \"${state.query}\"",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PredictionsList(
                predictions = state.predictions,
                onPredictionClick = { viewModel.onAction(PlacePickerAction.SelectPrediction(it)) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
internal fun ConfirmationDialogIfNeeded(
    state: PlacePickerState,
    config: PlacePickerConfig,
    viewModel: PlacePickerViewModel
) {
    if (state.showConfirmationDialog && state.selectedPrediction != null) {
        PlaceConfirmationDialog(
            prediction = state.selectedPrediction,
            place = state.selectedPlace,
            isLoading = state.isLoadingDetails,
            confirmButtonText = config.confirmButtonText,
            cancelButtonText = config.cancelButtonText,
            onConfirm = { viewModel.onAction(PlacePickerAction.ConfirmSelection) },
            onDismiss = { viewModel.onAction(PlacePickerAction.DismissConfirmation) }
        )
    }
}
