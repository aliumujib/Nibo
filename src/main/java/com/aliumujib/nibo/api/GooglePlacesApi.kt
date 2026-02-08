package com.aliumujib.nibo.api

import com.aliumujib.nibo.data.PlacePrediction
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Places API.
 * @suppress
 */
internal interface GooglePlacesApi {
    
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

// Internal response models

internal data class PlacesAutocompleteResponse(
    val predictions: List<PlacePrediction>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String?
)

internal data class PlaceDetailsResponse(
    val result: PlaceDetails?,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String?
)

internal data class PlaceDetails(
    val name: String,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: Geometry,
    @SerializedName("place_id")
    val placeId: String
)

internal data class Geometry(
    val location: LatLng
)

internal data class LatLng(
    val lat: Double,
    val lng: Double
)
