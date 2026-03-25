package com.amjb_apps.placepicker.sample.ui

import androidx.lifecycle.ViewModel
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.SelectedPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SampleAppState(
    val selectedPlace: SelectedPlace? = null,
    val showConfigSection: Boolean = false,
    val pickerConfig: PickerConfigState = PickerConfigState()
)

data class PickerConfigState(
    val title: String = "Select Location",
    val searchHint: String = "Search for a place",
    val confirmButtonText: String = "Confirm",
    val cancelButtonText: String = "Cancel",
    val placeType: String = "(regions)"
)

sealed interface SampleAppAction {
    data class UpdateSelectedPlace(val place: SelectedPlace?) : SampleAppAction
    data object ToggleConfigSection : SampleAppAction
    data class UpdateTitle(val title: String) : SampleAppAction
    data class UpdateSearchHint(val hint: String) : SampleAppAction
    data class UpdateConfirmButtonText(val text: String) : SampleAppAction
    data class UpdateCancelButtonText(val text: String) : SampleAppAction
    data class UpdatePlaceType(val type: String) : SampleAppAction
}

class PlacePickerSampleViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(SampleAppState())
    val state: StateFlow<SampleAppState> = _state.asStateFlow()
    
    fun onAction(action: SampleAppAction) {
        when (action) {
            is SampleAppAction.UpdateSelectedPlace -> {
                _state.update { it.copy(selectedPlace = action.place) }
            }
            is SampleAppAction.ToggleConfigSection -> {
                _state.update { it.copy(showConfigSection = !it.showConfigSection) }
            }
            is SampleAppAction.UpdateTitle -> {
                _state.update { 
                    it.copy(pickerConfig = it.pickerConfig.copy(title = action.title))
                }
            }
            is SampleAppAction.UpdateSearchHint -> {
                _state.update { 
                    it.copy(pickerConfig = it.pickerConfig.copy(searchHint = action.hint))
                }
            }
            is SampleAppAction.UpdateConfirmButtonText -> {
                _state.update { 
                    it.copy(pickerConfig = it.pickerConfig.copy(confirmButtonText = action.text))
                }
            }
            is SampleAppAction.UpdateCancelButtonText -> {
                _state.update { 
                    it.copy(pickerConfig = it.pickerConfig.copy(cancelButtonText = action.text))
                }
            }
            is SampleAppAction.UpdatePlaceType -> {
                _state.update { 
                    it.copy(pickerConfig = it.pickerConfig.copy(placeType = action.type))
                }
            }
        }
    }
    
}
