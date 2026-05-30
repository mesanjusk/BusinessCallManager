package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold

@Composable
fun TriStateToggle(homeVm: HomeVm) {
    val states = listOf(
        "Pop-Up",
        "Notification",
        "None",
    )
    var selectedOption by remember {
        mutableStateOf(states[homeVm.callerPopupType.intValue])
    }
    val onSelectionChange = { text: String ->
        selectedOption = text
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 5.dp, top = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(24.dp))
                .background(Color.LightGray)
        ) {
            states.forEachIndexed { index, text ->
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 10.sp.nonScaledSp,
                    fontFamily = montserrat_semibold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape = RoundedCornerShape(24.dp))
                        .clickable {
                            onSelectionChange(text)
                            homeVm.updateSettings(index)
                        }
                        .background(
                            if (text == selectedOption) {
                                ThemePurple
                            } else {
                                Color.LightGray
                            }
                        )
                        .padding(
                            vertical = 8.dp,
                            horizontal = 5.dp,
                        ),
                )
            }
        }
    }
}