package com.ruchitech.quicklinkcaller.ui.screens.otp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularButton
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularLoadingIndicator
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel.OtpVerifyVM
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.TextFieldBg
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_medium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun OtpVerifyUi(viewModel: OtpVerifyVM) {
    val circularLoadingIndicator by viewModel.circularLoadingIndicator.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimary),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
            ) {
                Spacer(modifier = Modifier.height(90.dp))
                Text(
                    text = "Almost There",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp.nonScaledSp,
                    color = TextColor,
                    fontFamily = montserrat_medium,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(25.dp))
                Text(
                    text = "Please enter the 6-digit code sent to your WhatsApp ${viewModel.argsMobileNumber} for verification.",
                    modifier = Modifier
                        .fillMaxWidth(),
                    fontFamily = montserrat,
                    fontSize = 14.sp.nonScaledSp,
                    lineHeight = 20.sp.nonScaledSp,
                    color = TextColor,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(45.dp))
                val (editValue, setEditValue) = remember { mutableStateOf("") }
                val otpLength = remember { 6 }
                val focusRequester = remember { FocusRequester() }

                val keyboard = LocalSoftwareKeyboardController.current
                TextField(
                    value = editValue,
                    onValueChange = {
                        if (it.length <= otpLength) {
                            setEditValue(it)
                        }
                    },
                    modifier = Modifier
                        .size(0.dp)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (0 until otpLength).map { index ->
                        viewModel.filledOtp.value = editValue
                        OtpCell(
                            modifier = Modifier
                                .size(45.dp)
                                .onFocusChanged {
                                    focusRequester.requestFocus()
                                }
                                .clickable {
                                    //   focusRequester.requestFocus()
                                    keyboard?.show()
                                }
                                .border(1.dp, TextFieldBg, shape = RoundedCornerShape(percent = 18))
                                .background(
                                    color = TextFieldBg,
                                    shape = RoundedCornerShape(percent = 18)
                                ),
                            value = editValue.getOrNull(index)?.toString() ?: "",
                            isCursorVisible = editValue.length == index
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        viewModel.validationCheck()
                    },
                    shape = RoundedCornerShape(percent = 18),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 28.dp)
                        .shadow(shape = RoundedCornerShape(percent = 0), elevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePurple)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "VERIFY",
                            color = Color.White,
                            fontSize = 18.sp.nonScaledSp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = montserrat_medium
                        )
                    }
                }
            }

        }

        if (!WindowInsets.isImeVisible) {
            CircularButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp), shape = CircleShape
            ) {
                viewModel.navigateUp()
            }
        }

        if (circularLoadingIndicator) CircularLoadingIndicator()
    }
}

@Composable
fun OtpCell(
    modifier: Modifier,
    value: String,
    isCursorVisible: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val (cursorSymbol, setCursorSymbol) = remember { mutableStateOf("") }

    LaunchedEffect(key1 = cursorSymbol, isCursorVisible) {
        if (isCursorVisible) {
            scope.launch {
                delay(350)
                setCursorSymbol(if (cursorSymbol.isEmpty()) "|" else "")
            }
        }
    }

    Box(
        modifier = modifier.background(
            color = Color.Transparent,
            shape = RoundedCornerShape(percent = 18)
        )
    ) {

        Text(
            text = if (isCursorVisible) cursorSymbol else value,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = montserrat_medium,
            color = TextColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
