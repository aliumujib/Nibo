package com.amjb_apps.placepicker.ui.state

data class SearchFieldState(
    val query: String,
    val hint: String,
    val isLoading: Boolean,
    val onQueryChange: (String) -> Unit,
    val onClear: () -> Unit
)
