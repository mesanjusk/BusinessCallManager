package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple

@Composable
fun SettingSwitchItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @StringRes title: Int,
    @StringRes description: Int,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = ThemePurple,
                uncheckedTrackColor = Color.White,
                uncheckedBorderColor = Color(0xFF979797),
                uncheckedThumbColor = Color(0xFF979797)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Test() {
    var item1EnabledState by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(NavySurface)
    ) {
        SettingSwitchItem(
            title = R.string.app_name,
            description = R.string.app_name,
            checked = item1EnabledState,
            onCheckedChange = { newState ->
                item1EnabledState = newState
            }
        )
    }
}