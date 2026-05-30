package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TextUnit.nonScaledSp
    @Composable
    get() = (this.value / androidx.compose.ui.platform.LocalDensity.current.fontScale).sp

val TextUnit.nonScaledDp
    @Composable
    get() = (this.value / androidx.compose.ui.platform.LocalDensity.current.density).dp
