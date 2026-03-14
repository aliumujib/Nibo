package com.amjb_apps.placepicker.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Places API.
 */
interface GooglePlacesApi {
    
    @GET("place/autocomplete/json")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("location") location: String? = null,
        @Query("radius") radius: Int? = null,
        @Query("language") language: String? = null,
        @Query("types") types: String? = null,
    ): PlacesAutocompleteResponse

    @GET("place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "name,geometry,formatted_address,place_id",
    ): PlaceDetailsResponse
}

// Response models

data class PlacesAutocompleteResponse(
    val predictions: List<PlacePrediction>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String?
)

data class PlacePrediction(
    @SerializedName("place_id")
    val placeId: String,
    val description: String,
    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormatting
)

data class StructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
    @SerializedName("secondary_text")
    val secondaryText: String?
)

data class PlaceDetailsResponse(
    val result: PlaceDetails?,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String?
)

data class PlaceDetails(
    val name: String,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: Geometry,
    @SerializedName("place_id")
    val placeId: String
)

data class Geometry(
    val location: LatLng
)

data class LatLng(
    val lat: Double,
    val lng: Double
)

// Place types constants
object PlaceTypes {
    const val REGIONS = "(regions)"
    const val CITIES = "(cities)"
    const val GEOCODE = "geocode"
    const val ADDRESS = "address"
    const val ESTABLISHMENT = "establishment"
}
