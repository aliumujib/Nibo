package com.aliumujib.nibo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumujib.nibo.api.PlacesApiClient
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.PlacePrediction
import com.aliumujib.nibo.data.SelectedPlace
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PlacePickerVM"

/**
 * ViewModel for the PlacePicker screen.
 * @suppress
 */
internal class PlacePickerViewModel(
    private val config: PlacePickerConfig
) : ViewModel() {

    private val apiClient = PlacesApiClient(config.apiKey)

    private val _state = MutableStateFlow(PlacePickerState(query = config.initialQuery))
    val state: StateFlow<PlacePickerState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        Log.d(TAG, "PlacePickerViewModel initialized")
        Log.d(TAG, "API Key configured: ${config.apiKey.isNotBlank()}")
        Log.d(TAG, "API Key (first 10 chars): ${config.apiKey.take(10)}...")
        
        if (config.initialQuery.isNotEmpty()) {
            searchPlaces(config.initialQuery)
        }
    }

    fun onAction(action: PlacePickerAction) {
        when (action) {
            is PlacePickerAction.UpdateQuery -> updateQuery(action.query)
            is PlacePickerAction.SelectPrediction -> selectPrediction(action.prediction)
            is PlacePickerAction.ConfirmSelection -> confirmSelection()
            is PlacePickerAction.DismissConfirmation -> dismissConfirmation()
            is PlacePickerAction.ClearError -> clearError()
            is PlacePickerAction.ClearQuery -> clearQuery()
        }
    }

    private fun updateQuery(query: String) {
        Log.d(TAG, "updateQuery: '$query'")
        _state.update { it.copy(query = query, error = null) }
        
        // Cancel previous search
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            Log.d(TAG, "Query empty, clearing predictions")
            _state.update { it.copy(predictions = emptyList(), isLoading = false) }
            return
        }

        // Debounce search
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            searchPlaces(query)
        }
    }

    private fun searchPlaces(query: String) {
        Log.d(TAG, "searchPlaces: '$query'")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val types = config.placeTypes.joinToString("|")
            Log.d(TAG, "Searching with types: $types")
            
            apiClient.searchPlaces(
                query = query,
                types = types,
                language = config.language,
                radius = config.radiusMeters
            ).fold(
                onSuccess = { predictions ->
                    Log.d(TAG, "Search success: ${predictions.size} predictions")
                    _state.update { 
                        it.copy(
                            predictions = predictions,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Search failed: ${error.message}", error)
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun selectPrediction(prediction: PlacePrediction) {
        _state.update { 
            it.copy(
                selectedPrediction = prediction,
                isLoadingDetails = true,
                showConfirmationDialog = true
            )
        }

        // Fetch place details
        viewModelScope.launch {
            apiClient.getPlaceDetails(prediction.placeId).fold(
                onSuccess = { place ->
                    _state.update { 
                        it.copy(
                            selectedPlace = place,
                            isLoadingDetails = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isLoadingDetails = false,
                            showConfirmationDialog = false,
                            selectedPrediction = null
                        )
                    }
                }
            )
        }
    }

    private fun confirmSelection() {
        // The selected place is already in state, just close the dialog
        // The calling code will read the selected place
        _state.update { it.copy(showConfirmationDialog = false) }
    }

    private fun dismissConfirmation() {
        _state.update { 
            it.copy(
                showConfirmationDialog = false,
                selectedPrediction = null,
                selectedPlace = null
            )
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun clearQuery() {
        _state.update { 
            it.copy(
                query = "",
                predictions = emptyList()
            )
        }
    }

    /**
     * Get the currently selected place (if confirmed).
     */
    fun getSelectedPlace(): SelectedPlace? = _state.value.selectedPlace
}

/**
 * Factory for creating PlacePickerViewModel with config.
 * @suppress
 */
internal class PlacePickerViewModelFactory(
    private val config: PlacePickerConfig
) {
    fun create(): PlacePickerViewModel = PlacePickerViewModel(config)
}
