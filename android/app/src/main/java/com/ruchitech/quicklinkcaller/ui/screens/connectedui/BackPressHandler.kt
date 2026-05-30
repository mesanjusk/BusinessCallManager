package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun BackPressHandler(
    onBackPressed: () -> Unit,
    dispatcher: OnBackPressedDispatcher
) {
   // BackHandler(onBackPressed = onBackPressed, dispatcher = dispatcher)
}
