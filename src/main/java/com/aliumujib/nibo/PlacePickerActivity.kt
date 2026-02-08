package com.aliumujib.nibo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.SelectedPlace
import com.aliumujib.nibo.ui.PlacePickerScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Activity that hosts the PlacePickerScreen.
 * Use [PlacePickerLauncher] for easy integration with Activity Result API.
 */
class PlacePickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val config = extractConfig()
        if (config == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlacePickerScreen(
                        config = config,
                        onPlaceSelected = { place ->
                            returnResult(place)
                        },
                        onDismiss = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun extractConfig(): PlacePickerConfig? {
        val apiKey = intent.getStringExtra(EXTRA_API_KEY) ?: return null
        return PlacePickerConfig(
            apiKey = apiKey,
            title = intent.getStringExtra(EXTRA_TITLE) ?: "Select Location",
            searchHint = intent.getStringExtra(EXTRA_SEARCH_HINT) ?: "Search for a place",
            confirmButtonText = intent.getStringExtra(EXTRA_CONFIRM_BUTTON) ?: "Confirm",
            cancelButtonText = intent.getStringExtra(EXTRA_CANCEL_BUTTON) ?: "Cancel",
            initialQuery = intent.getStringExtra(EXTRA_INITIAL_QUERY) ?: "",
            placeTypes = intent.getStringArrayListExtra(EXTRA_PLACE_TYPES) ?: listOf("(regions)"),
            language = intent.getStringExtra(EXTRA_LANGUAGE),
            radiusMeters = intent.getIntExtra(EXTRA_RADIUS, -1).takeIf { it > 0 }
        )
    }

    private fun returnResult(place: SelectedPlace) {
        val resultIntent = Intent().apply {
            putExtra(RESULT_PLACE_ID, place.placeId)
            putExtra(RESULT_PLACE_NAME, place.name)
            putExtra(RESULT_PLACE_ADDRESS, place.address)
            putExtra(RESULT_PLACE_LATITUDE, place.latitude)
            putExtra(RESULT_PLACE_LONGITUDE, place.longitude)
            putExtra(RESULT_PLACE_JSON, Json.encodeToString(place))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        // Input extras
        const val EXTRA_API_KEY = "api_key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_SEARCH_HINT = "search_hint"
        const val EXTRA_CONFIRM_BUTTON = "confirm_button"
        const val EXTRA_CANCEL_BUTTON = "cancel_button"
        const val EXTRA_INITIAL_QUERY = "initial_query"
        const val EXTRA_PLACE_TYPES = "place_types"
        const val EXTRA_LANGUAGE = "language"
        const val EXTRA_RADIUS = "radius"

        // Result extras
        const val RESULT_PLACE_ID = "place_id"
        const val RESULT_PLACE_NAME = "place_name"
        const val RESULT_PLACE_ADDRESS = "place_address"
        const val RESULT_PLACE_LATITUDE = "place_latitude"
        const val RESULT_PLACE_LONGITUDE = "place_longitude"
        const val RESULT_PLACE_JSON = "place_json"

        /**
         * Create an intent to launch the PlacePickerActivity.
         */
        fun createIntent(
            context: Context,
            apiKey: String,
            title: String = "Select Location",
            searchHint: String = "Search for a place",
            confirmButtonText: String = "Confirm",
            cancelButtonText: String = "Cancel",
            initialQuery: String = "",
            placeTypes: List<String> = listOf("(regions)"),
            language: String? = null,
            radiusMeters: Int? = null
        ): Intent {
            return Intent(context, PlacePickerActivity::class.java).apply {
                putExtra(EXTRA_API_KEY, apiKey)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SEARCH_HINT, searchHint)
                putExtra(EXTRA_CONFIRM_BUTTON, confirmButtonText)
                putExtra(EXTRA_CANCEL_BUTTON, cancelButtonText)
                putExtra(EXTRA_INITIAL_QUERY, initialQuery)
                putStringArrayListExtra(EXTRA_PLACE_TYPES, ArrayList(placeTypes))
                language?.let { putExtra(EXTRA_LANGUAGE, it) }
                radiusMeters?.let { putExtra(EXTRA_RADIUS, it) }
            }
        }

        /**
         * Parse the result from PlacePickerActivity.
         */
        fun parseResult(data: Intent?): SelectedPlace? {
            if (data == null) return null
            
            val json = data.getStringExtra(RESULT_PLACE_JSON)
            if (json != null) {
                return try {
                    Json.decodeFromString<SelectedPlace>(json)
                } catch (e: Exception) {
                    null
                }
            }

            // Fallback to individual extras
            val placeId = data.getStringExtra(RESULT_PLACE_ID) ?: return null
            val name = data.getStringExtra(RESULT_PLACE_NAME) ?: return null
            val address = data.getStringExtra(RESULT_PLACE_ADDRESS) ?: return null
            val latitude = data.getDoubleExtra(RESULT_PLACE_LATITUDE, 0.0)
            val longitude = data.getDoubleExtra(RESULT_PLACE_LONGITUDE, 0.0)

            return SelectedPlace(
                placeId = placeId,
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}
