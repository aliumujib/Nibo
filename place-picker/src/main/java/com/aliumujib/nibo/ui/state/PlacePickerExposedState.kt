package com.aliumujib.nibo.ui.state

import com.aliumujib.nibo.api.PlacePrediction

data class PlacePickerExposedState(
    val searchFieldState: SearchFieldState,
    val predictions: List<PlacePrediction>,
    val isLoading: Boolean,
    val error: String?
)
