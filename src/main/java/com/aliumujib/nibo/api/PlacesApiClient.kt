package com.aliumujib.nibo.api

import android.util.Log
import com.aliumujib.nibo.BuildConfig
import com.aliumujib.nibo.data.PlacePrediction
import com.aliumujib.nibo.data.PlaceTypes
import com.aliumujib.nibo.data.SelectedPlace
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client for interacting with Google Places API.
 * @suppress
 */
internal class PlacesApiClient(
    private val apiKey: String
) {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor { message ->
                    Log.d("PlacesAPI", message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
        }
        .build()

    private val api: GooglePlacesApi = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GooglePlacesApi::class.java)
    
    init {
        Log.d("PlacesAPI", "PlacesApiClient initialized with API key: ${apiKey.take(10)}...")
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

internal class PlacesApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
