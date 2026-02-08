package com.aliumujib.nibo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.aliumujib.nibo.data.SelectedPlace

/**
 * Input for the PlacePicker launcher.
 */
data class PlacePickerInput(
    val apiKey: String,
    val title: String = "Select Location",
    val searchHint: String = "Search for a place",
    val confirmButtonText: String = "Confirm",
    val cancelButtonText: String = "Cancel",
    val initialQuery: String = "",
    val placeTypes: List<String> = listOf("(regions)"),
    val language: String? = null,
    val radiusMeters: Int? = null
)

/**
 * Activity Result Contract for launching the PlacePicker.
 * 
 * Usage:
 * ```kotlin
 * val placePicker = rememberLauncherForActivityResult(PlacePickerContract()) { result ->
 *     result?.let { place ->
 *         // Handle selected place
 *     }
 * }
 * 
 * // Launch picker
 * placePicker.launch(PlacePickerInput(apiKey = "YOUR_API_KEY"))
 * ```
 */
class PlacePickerContract : ActivityResultContract<PlacePickerInput, SelectedPlace?>() {
    
    override fun createIntent(context: Context, input: PlacePickerInput): Intent {
        return PlacePickerActivity.createIntent(
            context = context,
            apiKey = input.apiKey,
            title = input.title,
            searchHint = input.searchHint,
            confirmButtonText = input.confirmButtonText,
            cancelButtonText = input.cancelButtonText,
            initialQuery = input.initialQuery,
            placeTypes = input.placeTypes,
            language = input.language,
            radiusMeters = input.radiusMeters
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): SelectedPlace? {
        if (resultCode != Activity.RESULT_OK) return null
        return PlacePickerActivity.parseResult(intent)
    }
}
