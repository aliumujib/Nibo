@file:Suppress("unused")

package com.aliumujib.nibo

/**
 * PlacePicker Library
 * 
 * A standalone, reusable Google Places autocomplete picker for Android Compose.
 * 
 * ## Features
 * - Full-screen place search with autocomplete
 * - Map confirmation dialog with preview
 * - Activity Result API integration
 * - Customizable UI text
 * - Support for various place types (regions, cities, addresses, etc.)
 * 
 * ## Quick Start
 * 
 * ### 1. Add Google Maps API Key to your app's AndroidManifest.xml
 * ```xml
 * <application>
 *     <meta-data
 *         android:name="com.google.android.geo.API_KEY"
 *         android:value="YOUR_GOOGLE_MAPS_API_KEY" />
 * </application>
 * ```
 * 
 * ### 2. Using with Activity Result API (Recommended)
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val placePicker = rememberLauncherForActivityResult(PlacePickerContract()) { place ->
 *         place?.let {
 *             // Handle selected place
 *             println("Selected: ${it.name} at ${it.latitude}, ${it.longitude}")
 *         }
 *     }
 *     
 *     Button(onClick = {
 *         placePicker.launch(PlacePickerInput(apiKey = "YOUR_API_KEY"))
 *     }) {
 *         Text("Select Location")
 *     }
 * }
 * ```
 * 
 * ### 3. Using as a Composable Screen
 * ```kotlin
 * PlacePickerScreen(
 *     config = PlacePickerConfig(
 *         apiKey = "YOUR_API_KEY",
 *         title = "Select Location"
 *     ),
 *     onPlaceSelected = { place ->
 *         // Handle selected place
 *     },
 *     onDismiss = {
 *         // Handle dismissal
 *     }
 * )
 * ```
 * 
 * ## Configuration Options
 * 
 * - `apiKey` - Google Places API key (required)
 * - `title` - Screen title
 * - `searchHint` - Search field placeholder
 * - `confirmButtonText` - Confirmation button text
 * - `cancelButtonText` - Cancel button text
 * - `initialQuery` - Pre-fill search query
 * - `placeTypes` - Filter by place types (e.g., "(regions)", "(cities)", "address")
 * - `language` - Language code for results
 * - `radiusMeters` - Bias results to a radius around a location
 * 
 * ## Place Types
 * 
 * Common place types:
 * - `(regions)` - Countries, states, cities, neighborhoods
 * - `(cities)` - Cities only
 * - `geocode` - Geocoding results
 * - `address` - Street addresses
 * - `establishment` - Businesses and POIs
 * 
 * @see PlacePickerContract
 * @see PlacePickerInput
 * @see PlacePickerConfig
 * @see SelectedPlace
 */
object PlacePicker {
    const val VERSION = "1.0.0"
}
