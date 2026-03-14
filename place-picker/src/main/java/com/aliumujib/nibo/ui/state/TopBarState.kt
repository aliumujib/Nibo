package com.aliumujib.nibo.ui.state

data class TopBarState(
    val title: String,
    val onBackClick: () -> Unit
)
