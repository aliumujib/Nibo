package com.amjb_apps.placepicker.sample.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliumujib.nibo.PlacePickerContract
import com.aliumujib.nibo.PlacePickerInput
import com.aliumujib.nibo.api.PlacesDataSource
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.SelectedPlace
import com.amjb_apps.placepicker.sample.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerSampleApp(
    viewModel: PlacePickerSampleViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val state by viewModel.state.collectAsState()
    
    val placePicker = rememberLauncherForActivityResult(PlacePickerContract()) { result ->
        viewModel.onAction(SampleAppAction.UpdateSelectedPlace(result))
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Place Picker")
                        Text(
                            text = "Sample App",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Configuration Toggle Button
            Button(
                onClick = { viewModel.onAction(SampleAppAction.ToggleConfigSection) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.showConfigSection) "Hide Configuration" else "Show Configuration")
            }
            
            // Configuration Section
            AnimatedVisibility(
                visible = state.showConfigSection,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ConfigurationSection(
                    config = state.pickerConfig,
                    onAction = viewModel::onAction
                )
            }
            
            // Launch Button
            Button(
                onClick = {
                    val config = state.pickerConfig
                    placePicker.launch(
                        PlacePickerInput(
                            apiKey = BuildConfig.GOOGLE_PLACES_API_KEY,
                            title = config.title,
                            searchHint = config.searchHint,
                            confirmButtonText = config.confirmButtonText,
                            cancelButtonText = config.cancelButtonText,
                            placeTypes = listOf(config.placeType)
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select a Place")
            }
            
            HorizontalDivider()
            
            // Result Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Place",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                AnimatedVisibility(visible = state.selectedPlace != null) {
                    IconButton(onClick = { viewModel.onAction(SampleAppAction.UpdateSelectedPlace(null)) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
            
            AnimatedVisibility(
                visible = state.selectedPlace != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.selectedPlace?.let { place ->
                    PlaceResultCard(place = place)
                }
            }
            
            AnimatedVisibility(
                visible = state.selectedPlace == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No place selected",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Reverse Geocoding Test Section
            HorizontalDivider()
            
            ReverseGeocodingTestSection(selectedPlace = state.selectedPlace)
        }
    }
}

@Composable
private fun PlaceResultCard(place: SelectedPlace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            PlaceDetailRow(label = "Place ID", value = place.placeId)
            PlaceDetailRow(label = "Latitude", value = "%.6f".format(place.latitude))
            PlaceDetailRow(label = "Longitude", value = "%.6f".format(place.longitude))
        }
    }
}

@Composable
private fun PlaceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ReverseGeocodingTestSection(selectedPlace: SelectedPlace?) {
    val scope = rememberCoroutineScope()
    val dataSource = remember { PlacesDataSource(BuildConfig.GOOGLE_PLACES_API_KEY) }
    
    var reverseGeocodedPlace by remember { mutableStateOf<SelectedPlace?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reverse Geocoding Test",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (selectedPlace != null) {
            Text(
                text = "Using coordinates from selected place:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Lat: %.6f, Lng: %.6f".format(selectedPlace.latitude, selectedPlace.longitude),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Select a place first to test reverse geocoding",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        Button(
            onClick = {
                selectedPlace?.let { place ->
                    isLoading = true
                    error = null
                    reverseGeocodedPlace = null
                    
                    scope.launch {
                        dataSource.reverseGeocode(
                            latitude = place.latitude,
                            longitude = place.longitude,
                            preferEstablishments = true
                        ).fold(
                            onSuccess = { geocodedPlace ->
                                reverseGeocodedPlace = geocodedPlace
                                isLoading = false
                            },
                            onFailure = { throwable ->
                                error = throwable.message
                                isLoading = false
                            }
                        )
                    }
                }
            },
            enabled = !isLoading && selectedPlace != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reverse Geocoding...")
            } else {
                Text("Test Reverse Geocoding")
            }
        }
        
        // Show error if any
        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Show result if available
        AnimatedVisibility(
            visible = reverseGeocodedPlace != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            reverseGeocodedPlace?.let { place ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓ Reverse Geocoded Result",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                        )
                        
                        PlaceDetailRow(label = "Name", value = place.name)
                        PlaceDetailRow(label = "Address", value = place.address)
                        PlaceDetailRow(label = "Place ID", value = place.placeId)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationSection(
    config: PickerConfigState,
    onAction: (SampleAppAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Picker Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider()
            
            // Title
            OutlinedTextField(
                value = config.title,
                onValueChange = { onAction(SampleAppAction.UpdateTitle(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Search Hint
            OutlinedTextField(
                value = config.searchHint,
                onValueChange = { onAction(SampleAppAction.UpdateSearchHint(it)) },
                label = { Text("Search Hint") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Confirm Button Text
            OutlinedTextField(
                value = config.confirmButtonText,
                onValueChange = { onAction(SampleAppAction.UpdateConfirmButtonText(it)) },
                label = { Text("Confirm Button Text") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Cancel Button Text
            OutlinedTextField(
                value = config.cancelButtonText,
                onValueChange = { onAction(SampleAppAction.UpdateCancelButtonText(it)) },
                label = { Text("Cancel Button Text") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Place Type Dropdown
            var expanded by remember { mutableStateOf(false) }
            val placeTypes = listOf(
                "(regions)" to "Regions",
                "(cities)" to "Cities",
                "geocode" to "Geocode",
                "address" to "Address",
                "establishment" to "Establishment"
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = placeTypes.find { it.first == config.placeType }?.second ?: config.placeType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Place Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    placeTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onAction(SampleAppAction.UpdatePlaceType(value))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
