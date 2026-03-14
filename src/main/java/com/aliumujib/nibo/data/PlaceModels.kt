package com.aliumujib.nibo.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Represents a selected place from the place picker.
 */
@Serializable
data class SelectedPlace(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Configuration for the PlacePicker.
 */
data class PlacePickerConfig(
    val apiKey: String,
    val title: String = "Select Location",
    val searchHint: String = "Search for a place",
    val confirmButtonText: String = "Confirm",
    val cancelButtonText: String = "Cancel",
    val initialQuery: String = "",
    val showCurrentLocation: Boolean = true,
    val placeTypes: List<String> = listOf(PlaceTypes.REGIONS),
    val language: String? = null,
    val radiusMeters: Int? = null
)

/**
 * Represents a place prediction from Google Places autocomplete.
 */
data class PlacePrediction(
    @SerializedName("place_id")
    val placeId: String,
    val description: String,
    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormatting
)

/**
 * Structured formatting for place prediction display.
 */
data class StructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
    @SerializedName("secondary_text")
    val secondaryText: String?
)

/**
 * Constants for place type filters.
 */
object PlaceTypes {
    const val REGIONS = "(regions)"
    const val CITIES = "(cities)"
    const val GEOCODE = "geocode"
    const val ADDRESS = "address"
    const val ESTABLISHMENT = "establishment"
}
