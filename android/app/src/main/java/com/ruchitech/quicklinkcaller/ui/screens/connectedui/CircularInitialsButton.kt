package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold

@Composable
fun CircularInitialsButton(isActive: Boolean = false, initials: String, onClick: () -> Unit) {
    val interactionSource =
        remember { MutableInteractionSource() }
    Box(modifier = Modifier.size(50.dp)) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape) // Clip the Box to be circular
                .border(2.dp, if (isActive) Color(0xFF49C370) else Color.Gray, CircleShape)
                .background(color = Color(0xFFA55CD2), shape = CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )

                    detectDragGestures { change, dragAmount ->
                        change.consume()
                    }
                }
                .padding(if (isActive) 2.dp else 2.dp)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {


            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape) // Clip the Box to be circular
                    .border(if (isActive) 2.dp else 0.dp, TextColor, CircleShape)
                    .background(color = PurpleSolid, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )

                        detectDragGestures { change, dragAmount ->
                            change.consume()
                        }
                    }
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 14.sp.nonScaledSp,
                    fontFamily = montserrat_semibold,
                    style = MaterialTheme.typography.bodySmall
                )

            }


        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape) // Clip the Box to be circular
                    .border(2.dp, TextColor, CircleShape)
                    .background(color = Color(0xFF49C370), shape = CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )

                        detectDragGestures { change, dragAmount ->
                            change.consume()
                        }
                    }
                    .padding(5.dp)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {}

        }

    }

}


@Composable
fun CircularInitialsButton2(isActive: Boolean = false, initials: String, onClick: () -> Unit) {
    val interactionSource =
        remember { MutableInteractionSource() }
    val state = rememberDraggableState(
        onDelta = { delta -> Log.d("kmjiunhbygtfdr", "Dragged $delta") }
    )
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.Transparent), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape) // Clip the Box to be circular
                .border(0.dp, if (isActive) Color(0xFF49C370) else Color.Gray, CircleShape)
                .background(color = Color(0xFFA55CD2), shape = CircleShape)
                .padding(if (isActive) 0.dp else 0.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {


            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape) // Clip the Box to be circular
                    .background(color = Color(0xFFA55CD2), shape = CircleShape)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(65.dp).clip(CircleShape)
                )
            }
        }


        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .height(30.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape) // Clip the Box to be circular
                    .border(0.5.dp, TextColor, CircleShape)
                    .background(color = Color(0xFF49C370), shape = CircleShape)
                    .padding(0.dp)
                    .clickable {
                        onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 14.sp.nonScaledSp,
                    fontFamily = montserrat_semibold,
                    style = MaterialTheme.typography.bodySmall
                )

            }
        }


    }

}

@Composable
fun DraggableContent(onLong: (x: Float, y: Float) -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var setEnb by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        if (setEnb) {
                            onLong(offsetX, offsetY)
                        }
                    }
                    detectTapGestures(onTap = {
                        Log.e("jhgngbdv", "DraggableContent: tap")
                    }, onLongPress = {
                        Log.e("jhgngbdv", "DraggableContent: long press")
                        setEnb = true
                    }, onPress = {
                        Log.e("jhgngbdv", "DraggableContent: press ${this.tryAwaitRelease()}")
                        setEnb = false
                    })
                }
                .size(50.dp)
                .background(Color.Red)

        )
        Text(text = "Offset X $offsetX \nOffset Y: $offsetY")
    }
}