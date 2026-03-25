package com.aliumujib.nibo.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Internal Retrofit interface for Google Places API.
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
    
    @GET("geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String,
        @Query("result_type") resultType: String? = null,
        @Query("location_type") locationType: String? = null,
        @Query("language") language: String? = null,
    ): GeocodeResponse
}

// Response models

internal data class PlacesAutocompleteResponse(
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

// Reverse geocoding response models

internal data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String?
)

internal data class GeocodeResult(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("address_components")
    val addressComponents: List<AddressComponent>,
    val geometry: Geometry,
    val types: List<String>
)

internal data class AddressComponent(
    @SerializedName("long_name")
    val longName: String,
    @SerializedName("short_name")
    val shortName: String,
    val types: List<String>
)

// Place types constants
internal object PlaceTypes {
    const val REGIONS = "(regions)"
    const val CITIES = "(cities)"
    const val GEOCODE = "geocode"
    const val ADDRESS = "address"
    const val ESTABLISHMENT = "establishment"
}

// Geocoding result type constants (for filtering reverse geocoding results)
internal object GeocodeResultType {
    const val STREET_ADDRESS = "street_address"
    const val ROUTE = "route"
    const val INTERSECTION = "intersection"
    const val POLITICAL = "political"
    const val COUNTRY = "country"
    const val ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1"
    const val ADMINISTRATIVE_AREA_LEVEL_2 = "administrative_area_level_2"
    const val ADMINISTRATIVE_AREA_LEVEL_3 = "administrative_area_level_3"
    const val ADMINISTRATIVE_AREA_LEVEL_4 = "administrative_area_level_4"
    const val ADMINISTRATIVE_AREA_LEVEL_5 = "administrative_area_level_5"
    const val LOCALITY = "locality"
    const val SUBLOCALITY = "sublocality"
    const val NEIGHBORHOOD = "neighborhood"
    const val PREMISE = "premise"
    const val SUBPREMISE = "subpremise"
    const val POSTAL_CODE = "postal_code"
    const val NATURAL_FEATURE = "natural_feature"
    const val AIRPORT = "airport"
    const val PARK = "park"
    const val POINT_OF_INTEREST = "point_of_interest"
    const val ESTABLISHMENT = "establishment"
}

// Geocoding location type constants (for filtering reverse geocoding results)
internal object GeocodeLocationType {
    const val ROOFTOP = "ROOFTOP"
    const val RANGE_INTERPOLATED = "RANGE_INTERPOLATED"
    const val GEOMETRIC_CENTER = "GEOMETRIC_CENTER"
    const val APPROXIMATE = "APPROXIMATE"
}
