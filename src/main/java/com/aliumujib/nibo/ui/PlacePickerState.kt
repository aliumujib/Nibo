package com.aliumujib.nibo.ui

import com.aliumujib.nibo.data.PlacePrediction
import com.aliumujib.nibo.data.SelectedPlace

/**
 * UI state for the PlacePicker screen.
 * @suppress
 */
internal data class PlacePickerState(
    val query: String = "",
    val predictions: List<PlacePrediction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPrediction: PlacePrediction? = null,
    val selectedPlace: SelectedPlace? = null,
    val isLoadingDetails: Boolean = false,
    val showConfirmationDialog: Boolean = false
)

/**
 * Actions for the PlacePicker screen.
 * @suppress
 */
internal sealed interface PlacePickerAction {
    data class UpdateQuery(val query: String) : PlacePickerAction
    data class SelectPrediction(val prediction: PlacePrediction) : PlacePickerAction
    data object ConfirmSelection : PlacePickerAction
    data object DismissConfirmation : PlacePickerAction
    data object ClearError : PlacePickerAction
    data object ClearQuery : PlacePickerAction
}
