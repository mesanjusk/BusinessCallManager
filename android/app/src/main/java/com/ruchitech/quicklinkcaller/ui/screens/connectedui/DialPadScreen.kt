package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import kotlinx.coroutines.delay
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid

fun View.vibrate() = reallyPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
fun View.vibrateStrong() = reallyPerformHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

private fun View.reallyPerformHapticFeedback(feedbackConstant: Int) {
    if (context.isTouchExplorationEnabled()) {
        // Don't mess with a blind person's vibrations
        return
    }
    // Either this needs to be set to true, or android:hapticFeedbackEnabled="true" needs to be set in XML
    isHapticFeedbackEnabled = true

    // Most of the constants are off by default: for example, clicking on a button doesn't cause the phone to vibrate anymore
    // if we still want to access this vibration, we'll have to ignore the global settings on that.
    performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}

private fun Context.isTouchExplorationEnabled(): Boolean {
    // can be null during unit tests
    val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
    return accessibilityManager?.isTouchExplorationEnabled ?: false
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialPadScreen(
    onNumberChange: (number: String) -> Unit,
    onActionDial: (number: String) -> Unit
) {
    val view = LocalView.current
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current // Get haptic feedback instance

    LaunchedEffect(isPressed) {
        if (isPressed) {
            while (isPressed && phoneNumber.isNotEmpty()) {
                phoneNumber = phoneNumber.dropLast(1)
                Log.e("iohuiyhuohuo", "DialPadScreen: clicked")
               view.vibrate()
                onNumberChange(phoneNumber) // Remove last digit
                delay(200) // Adjust speed of deletion
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(NavyPrimary)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display entered phone number
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = phoneNumber.ifEmpty { "Enter Number" },
                fontSize = 32.sp,
                color = Color.Black.copy(alpha = 0.8F),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.Center)
            )

            /*
                        Image(
                            painter = painterResource(R.drawable.ic_delete_digits),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    phoneNumber = phoneNumber.dropLast(1)
                                    onNumberChange(phoneNumber)
                                }
                        )*/

            Image(
                painter = painterResource(R.drawable.ic_delete_digits),
                contentDescription = "Delete",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterEnd)
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> isPressed = true
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
                        }
                        true
                    }
            )


        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dialpad Buttons
        DialPad(
            onNumberClick = { number ->
                if (phoneNumber.length < 15) phoneNumber += number
                Log.e("gljfhgkfhfg", "DialPadScreen: ")
                       view.vibrate()
                /*         haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Vibrate*/
                onNumberChange(phoneNumber)
            },
            onBackspaceClick = {
                phoneNumber = phoneNumber.dropLast(1)

                onNumberChange(phoneNumber)
            },
            onCallClick = {
                onActionDial(phoneNumber)
            }
        )
    }
}

@Composable
fun DialPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val buttons = listOf(
        listOf(Triple("1", "", ""), Triple("2", "ABC", ""), Triple("3", "DEF", "")),
        listOf(Triple("4", "GHI", ""), Triple("5", "JKL", ""), Triple("6", "MNO", "")),
        listOf(Triple("7", "PQRS", ""), Triple("8", "TUV", ""), Triple("9", "WXYZ", "")),
        listOf(Triple("*", "", ""), Triple("0", "+", ""), Triple("#", "", ""))
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        buttons.forEach { row ->
            Row {
                row.forEach { (number, letters, _) ->
                    DialButton(number, letters, onClick = { onNumberClick(number) })
                }
            }
        }

        // Bottom row (Call & Backspace)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            onCallClick()
        }, colors = ButtonDefaults.buttonColors(containerColor = PurpleSolid)) {
            Text("Call", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp.nonScaledSp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
/*
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable {
                    onCallClick()
                },
            horizontalArrangement = Arrangement.Center
        ) {
            // Call Button
            Image(
                painter = painterResource(R.drawable.ic_dial),   // Replace with an actual call icon
                contentDescription = "Call",
                modifier = Modifier.size(45.dp),
            )

            */
/*            // Backspace Button
                      Box(
                          modifier = Modifier
                              .size(80.dp)
                              .clip(CircleShape)
                              .background(Color.Red)
                              .clickable(onClick = onBackspaceClick)
                          // .shadow(1.dp, shape = CircleShape),
                          , contentAlignment = Alignment.Center
                      ) {
                          Icon(
                              painter = painterResource(R.drawable.minus), // Replace with an actual backspace icon
                              contentDescription = "Backspace",
                              modifier = Modifier.size(80.dp),
                              tint = Color.White
                          )
                      }*//*


        }
*/

    }
}

@Composable
fun DialButton(number: String, letters: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(125.dp)
            .padding(vertical = 8.dp)
            .background(NavyPrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontSize = 32.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
         //   if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
          //  }
        }
    }
}





