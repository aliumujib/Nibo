package com.amjb_apps.placepicker.data

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
    val placeTypes: List<String> = listOf("(regions)"),
    val language: String? = null,
    val radiusMeters: Int? = null
)
