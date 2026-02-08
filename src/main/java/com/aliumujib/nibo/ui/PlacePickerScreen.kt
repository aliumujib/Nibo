package com.aliumujib.nibo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliumujib.nibo.data.PlacePickerConfig
import com.aliumujib.nibo.data.PlacePrediction
import com.aliumujib.nibo.data.SelectedPlace
import com.aliumujib.nibo.ui.components.PlaceConfirmationDialog

/**
 * Full-screen place picker composable.
 * 
 * @param config Configuration for the place picker
 * @param onPlaceSelected Callback when a place is confirmed
 * @param onDismiss Callback when the picker is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerScreen(
    config: PlacePickerConfig,
    onPlaceSelected: (SelectedPlace) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(config) { PlacePickerViewModelFactory(config).create() }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Show error in snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onAction(PlacePickerAction.ClearError)
        }
    }

    // Handle confirmed selection
    LaunchedEffect(state.selectedPlace, state.showConfirmationDialog) {
        if (state.selectedPlace != null && !state.showConfirmationDialog) {
            // User confirmed the selection
            onPlaceSelected(state.selectedPlace!!)
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(config.title) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            SearchField(
                query = state.query,
                hint = config.searchHint,
                isLoading = state.isLoading,
                onQueryChange = { viewModel.onAction(PlacePickerAction.UpdateQuery(it)) },
                onClear = { viewModel.onAction(PlacePickerAction.ClearQuery) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Results list
            if (state.predictions.isEmpty() && state.query.isNotEmpty() && !state.isLoading) {
                EmptyState(
                    message = "No places found for \"${state.query}\"",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PredictionsList(
                    predictions = state.predictions,
                    onPredictionClick = { viewModel.onAction(PlacePickerAction.SelectPrediction(it)) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Confirmation dialog
    if (state.showConfirmationDialog && state.selectedPrediction != null) {
        PlaceConfirmationDialog(
            prediction = state.selectedPrediction!!,
            place = state.selectedPlace,
            isLoading = state.isLoadingDetails,
            confirmButtonText = config.confirmButtonText,
            cancelButtonText = config.cancelButtonText,
            onConfirm = { viewModel.onAction(PlacePickerAction.ConfirmSelection) },
            onDismiss = { viewModel.onAction(PlacePickerAction.DismissConfirmation) }
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    hint: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(hint) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = isLoading || query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun PredictionsList(
    predictions: List<PlacePrediction>,
    onPredictionClick: (PlacePrediction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier
    ) {
        items(
            items = predictions,
            key = { it.placeId }
        ) { prediction ->
            PredictionItem(
                prediction = prediction,
                onClick = { onPredictionClick(prediction) }
            )
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: PlacePrediction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        leadingContent = {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        headlineContent = {
            Text(
                text = prediction.structuredFormatting.mainText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = prediction.structuredFormatting.secondaryText?.let { secondary ->
            {
                Text(
                    text = secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
