package com.aliumujib.nibo.api

import android.util.Log
import com.aliumujib.nibo.data.SelectedPlace
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Data source for interacting with Google Places API.
 * Handles autocomplete, place details, and reverse geocoding.
 */
class PlacesDataSource(
    private val apiKey: String
) {
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("PlacesAPI", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val api: GooglePlacesApi = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GooglePlacesApi::class.java)
    
    init {
        Log.d("PlacesAPI", "PlacesDataSource initialized with API key: ${apiKey.take(10)}...")
    }

    /**
     * Search for places based on a query string.
     */
    suspend fun searchPlaces(
        query: String,
        types: String? = PlaceTypes.REGIONS,
        location: String? = null,
        radius: Int? = null,
        language: String? = null
    ): Result<List<PlacePrediction>> {
        Log.d("PlacesAPI", "searchPlaces called with query: '$query', types: $types")
        
        if (query.length < 2) {
            Log.d("PlacesAPI", "Query too short, returning empty list")
            return Result.success(emptyList())
        }

        if (apiKey.isBlank()) {
            Log.e("PlacesAPI", "API key is blank!")
            return Result.failure(PlacesApiException("API key is not configured"))
        }

        return try {
            Log.d("PlacesAPI", "Making autocomplete request...")
            val response = api.autocomplete(
                input = query,
                apiKey = apiKey,
                types = types,
                location = location,
                radius = radius,
                language = language
            )

            Log.d("PlacesAPI", "Response status: ${response.status}, predictions: ${response.predictions.size}")
            
            when (response.status) {
                "OK" -> {
                    Log.d("PlacesAPI", "Success! Found ${response.predictions.size} predictions")
                    Result.success(response.predictions)
                }
                "ZERO_RESULTS" -> {
                    Log.d("PlacesAPI", "Zero results for query: $query")
                    Result.success(emptyList())
                }
                "REQUEST_DENIED" -> {
                    Log.e("PlacesAPI", "Request denied: ${response.errorMessage}")
                    Result.failure(PlacesApiException("API request denied: ${response.errorMessage}"))
                }
                "INVALID_REQUEST" -> {
                    Log.e("PlacesAPI", "Invalid request: ${response.errorMessage}")
                    Result.failure(PlacesApiException("Invalid request: ${response.errorMessage}"))
                }
                else -> {
                    Log.e("PlacesAPI", "Unknown error: ${response.status} - ${response.errorMessage}")
                    Result.failure(
                        PlacesApiException(response.errorMessage ?: "Unknown error: ${response.status}")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PlacesAPI", "Network error: ${e.message}", e)
            Result.failure(PlacesApiException("Network error: ${e.message}", e))
        }
    }

    /**
     * Reverse geocode coordinates to get place details.
     * Returns full SelectedPlace with name, address, and coordinates.
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param preferEstablishments If true, prioritizes businesses/POIs over addresses
     * @param language Optional language code (e.g., "en", "es")
     * @return Result with SelectedPlace if found, failure otherwise
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double,
        preferEstablishments: Boolean = true,
        language: String? = null
    ): Result<SelectedPlace> {
        Log.d("PlacesAPI", "reverseGeocode called with lat: $latitude, lng: $longitude")
        
        if (apiKey.isBlank()) {
            Log.e("PlacesAPI", "API key is blank!")
            return Result.failure(PlacesApiException("API key is not configured"))
        }

        return try {
            val latLng = "$latitude,$longitude"
            Log.d("PlacesAPI", "Making reverse geocode request for: $latLng")
            
            val response = api.reverseGeocode(
                latLng = latLng,
                apiKey = apiKey,
                language = language
            )

            Log.d("PlacesAPI", "Response status: ${response.status}, results: ${response.results.size}")
            
            when (response.status) {
                "OK" -> {
                    val result = if (preferEstablishments) {
                        // Prefer named places (POI, businesses) over generic addresses
                        response.results.firstOrNull { result ->
                            result.types.any { type ->
                                type in listOf(
                                    "point_of_interest",
                                    "establishment",
                                    "premise",
                                    "subpremise"
                                )
                            }
                        } ?: response.results.firstOrNull()
                    } else {
                        response.results.firstOrNull()
                    }
                    
                    if (result != null) {
                        val placeName = extractPlaceName(result)
                        Log.d("PlacesAPI", "Reverse geocode success: $placeName")
                        
                        Result.success(
                            SelectedPlace(
                                placeId = result.placeId,
                                name = placeName,
                                address = result.formattedAddress,
                                latitude = latitude,
                                longitude = longitude
                            )
                        )
                    } else {
                        Log.w("PlacesAPI", "No results found for coordinates")
                        Result.failure(PlacesApiException("No place found at coordinates"))
                    }
                }
                "ZERO_RESULTS" -> {
                    Log.w("PlacesAPI", "No results for coordinates: $latLng")
                    Result.failure(PlacesApiException("No place found at coordinates"))
                }
                "REQUEST_DENIED" -> {
                    Log.e("PlacesAPI", "Request denied: ${response.errorMessage}")
                    Result.failure(PlacesApiException("API request denied: ${response.errorMessage}"))
                }
                "INVALID_REQUEST" -> {
                    Log.e("PlacesAPI", "Invalid request: ${response.errorMessage}")
                    Result.failure(PlacesApiException("Invalid coordinates or parameters"))
                }
                "OVER_QUERY_LIMIT" -> {
                    Log.e("PlacesAPI", "API quota exceeded")
                    Result.failure(PlacesApiException("API quota exceeded. Try again later."))
                }
                else -> {
                    Log.e("PlacesAPI", "Unknown error: ${response.status} - ${response.errorMessage}")
                    Result.failure(
                        PlacesApiException(response.errorMessage ?: "Unknown error: ${response.status}")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PlacesAPI", "Network error during reverse geocode: ${e.message}", e)
            Result.failure(PlacesApiException("Network error: ${e.message}", e))
        }
    }

    /**
     * Extract a meaningful place name from geocode result.
     * Priority: POI name > Premise > Street address > Locality
     */
    private fun extractPlaceName(result: GeocodeResult): String {
        // 1. Check if there's a specific establishment/POI name
        result.addressComponents.find { component ->
            component.types.contains("establishment") || 
            component.types.contains("point_of_interest")
        }?.let { return it.longName }
        
        // 2. Check for premise (building name)
        result.addressComponents.find { component ->
            component.types.contains("premise")
        }?.let { return it.longName }
        
        // 3. Check for street number + route (street address)
        val streetNumber = result.addressComponents.find { 
            it.types.contains("street_number") 
        }?.longName
        val route = result.addressComponents.find { 
            it.types.contains("route") 
        }?.longName
        
        if (streetNumber != null && route != null) {
            return "$streetNumber $route"
        }
        if (route != null) return route
        
        // 4. Fall back to locality (neighborhood/city)
        result.addressComponents.find { component ->
            component.types.contains("locality") ||
            component.types.contains("sublocality") ||
            component.types.contains("neighborhood")
        }?.let { return it.longName }
        
        // 5. Last resort: use formatted address or coordinates label
        return result.formattedAddress.substringBefore(',').trim()
    }

    /**
     * Get details for a specific place by its ID.
     */
    suspend fun getPlaceDetails(placeId: String): Result<SelectedPlace> {
        Log.d("PlacesAPI", "getPlaceDetails called with placeId: $placeId")
        
        return try {
            val response = api.getPlaceDetails(
                placeId = placeId,
                apiKey = apiKey
            )

            Log.d("PlacesAPI", "Place details response status: ${response.status}")
            
            when (response.status) {
                "OK" -> {
                    val result = response.result
                    if (result != null) {
                        Log.d("PlacesAPI", "Place details found: ${result.name}")
                        Result.success(
                            SelectedPlace(
                                placeId = result.placeId,
                                name = result.name,
                                address = result.formattedAddress,
                                latitude = result.geometry.location.lat,
                                longitude = result.geometry.location.lng
                            )
                        )
                    } else {
                        Log.e("PlacesAPI", "Place details result is null")
                        Result.failure(PlacesApiException("Place details not found"))
                    }
                }
                else -> {
                    Log.e("PlacesAPI", "Place details error: ${response.status} - ${response.errorMessage}")
                    Result.failure(
                        PlacesApiException(response.errorMessage ?: "Unknown error: ${response.status}")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PlacesAPI", "Network error getting place details: ${e.message}", e)
            Result.failure(PlacesApiException("Network error: ${e.message}", e))
        }
    }
}

class PlacesApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
