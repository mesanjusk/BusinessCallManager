package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple

@Composable
fun CircularButton(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    color: Color = ThemePurple,
    shape: Shape = MaterialTheme.shapes.small,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowLeft,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(45.dp)
        )
    }
}
