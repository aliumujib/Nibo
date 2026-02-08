# Nibo

[![Release](https://jitpack.io/v/aliumujib/Nibo.svg)](https://jitpack.io/#aliumujib/Nibo)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Google Places-powered location picker for Jetpack Compose.

## Features

- Full-screen place picker with search autocomplete
- Map preview with selected location
- Confirmation dialog before selection
- Customizable text, place types, and language
- Supports both Composable and Activity Result API usage
- Material 3 design

## Installation

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.aliumujib:Nibo:3.0.0")
}
```

<details>
<summary>Groovy DSL</summary>

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    implementation 'com.github.aliumujib:Nibo:3.0.0'
}
```

</details>

## Setup

### Google Maps API Key

1. Get an API key from the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/credentials)
2. Enable the **Places API** and **Maps SDK for Android**
3. Add the API key to your `AndroidManifest.xml`:

```xml
<manifest>
    <application>
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${MAPS_API_KEY}" />
    </application>
</manifest>
```

4. Add the key to your `local.properties`:

```properties
MAPS_API_KEY=your_api_key_here
```

5. Reference it in your `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = 
            project.findProperty("MAPS_API_KEY") ?: ""
    }
}
```

## Usage

### Using as a Composable

```kotlin
import com.aliumujib.nibo.ui.PlacePickerScreen
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.SelectedPlace

@Composable
fun MyScreen() {
    var showPicker by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<SelectedPlace?>(null) }

    if (showPicker) {
        PlacePickerScreen(
            config = PlacePickerConfig(
                apiKey = BuildConfig.MAPS_API_KEY,
                title = "Select Location",
                searchHint = "Search for a place"
            ),
            onPlaceSelected = { place ->
                selectedPlace = place
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
```

### Using Activity Result API

```kotlin
import com.aliumujib.nibo.PlacePickerContract
import com.aliumujib.nibo.PlacePickerInput

@Composable
fun MyScreen() {
    val launcher = rememberLauncherForActivityResult(PlacePickerContract()) { place ->
        place?.let {
            // Handle selected place
            println("Selected: ${it.name} at ${it.latitude}, ${it.longitude}")
        }
    }

    Button(onClick = {
        launcher.launch(PlacePickerInput(apiKey = BuildConfig.MAPS_API_KEY))
    }) {
        Text("Pick Location")
    }
}
```

### Using from Activity/Fragment

```kotlin
import com.aliumujib.nibo.PlacePickerActivity
import com.aliumujib.nibo.PlacePickerInput

class MyActivity : AppCompatActivity() {
    
    private val placePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = PlacePickerActivity.parseResult(result.data)
            // Handle selected place
        }
    }

    fun openPicker() {
        val intent = PlacePickerActivity.createIntent(
            context = this,
            input = PlacePickerInput(apiKey = BuildConfig.MAPS_API_KEY)
        )
        placePickerLauncher.launch(intent)
    }
}
```

## Configuration

### PlacePickerConfig

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `apiKey` | `String` | Required | Google Places API key |
| `title` | `String` | "Select Location" | Screen title |
| `searchHint` | `String` | "Search for a place" | Search field placeholder |
| `confirmButtonText` | `String` | "Confirm" | Confirmation button text |
| `cancelButtonText` | `String` | "Cancel" | Cancel button text |
| `initialQuery` | `String` | "" | Pre-fill search query |
| `placeTypes` | `List<String>` | `["(regions)"]` | Place type filters |
| `language` | `String?` | `null` | Language code for results |
| `radiusMeters` | `Int?` | `null` | Bias results to radius |

### Place Types

Use the `PlaceTypes` constants:

```kotlin
import com.aliumujib.nibo.data.PlaceTypes

PlacePickerConfig(
    apiKey = "...",
    placeTypes = listOf(PlaceTypes.CITIES, PlaceTypes.REGIONS)
)
```

Available types:
- `PlaceTypes.REGIONS` - Regions (default)
- `PlaceTypes.CITIES` - Cities
- `PlaceTypes.GEOCODE` - Geocode results
- `PlaceTypes.ADDRESS` - Addresses
- `PlaceTypes.ESTABLISHMENT` - Establishments/businesses

### SelectedPlace

The result contains:

```kotlin
data class SelectedPlace(
    val placeId: String,      // Google Place ID
    val name: String,         // Place name
    val address: String,      // Formatted address
    val latitude: Double,     // Latitude
    val longitude: Double     // Longitude
)
```

## Requirements

- Android API 26+
- Jetpack Compose
- Google Maps API key with Places API enabled

## License

```
MIT License

Copyright (c) 2016-2026 Aliu Abdul-Mujeeb

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
