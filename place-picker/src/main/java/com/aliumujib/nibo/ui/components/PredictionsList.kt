package com.aliumujib.nibo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliumujib.nibo.api.PlacePrediction

@Composable
internal fun PredictionsList(
    predictions: List<PlacePrediction>,
    onPredictionClick: (PlacePrediction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier
    ) {
        items(items = predictions, key = { it.placeId }) { prediction ->
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
                prediction.structuredFormatting.mainText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = prediction.structuredFormatting.secondaryText?.let { secondary ->
            {
                Text(
                    secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(onClick = onClick)
    )
}
