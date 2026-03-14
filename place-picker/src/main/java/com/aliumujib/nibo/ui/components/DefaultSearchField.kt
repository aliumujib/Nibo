package com.aliumujib.nibo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliumujib.nibo.R
import com.aliumujib.nibo.ui.state.SearchFieldState

@Composable
fun DefaultSearchField(
    state: SearchFieldState,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = state.query,
        onValueChange = state.onQueryChange,
        placeholder = { Text(state.hint) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(
                visible = state.isLoading || state.query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (state.query.isNotEmpty()) {
                    IconButton(onClick = state.onClear) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.place_picker_clear)
                        )
                    }
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun DefaultSearchField(
    query: String,
    hint: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    DefaultSearchField(
        state = SearchFieldState(query, hint, isLoading, onQueryChange, onClear),
        modifier = modifier
    )
}
