package com.amjb_apps.placepicker.ui.state

import com.amjb_apps.placepicker.api.PlacePrediction

data class PlacePickerExposedState(
    val searchFieldState: SearchFieldState,
    val predictions: List<PlacePrediction>,
    val isLoading: Boolean,
    val error: String?
)
