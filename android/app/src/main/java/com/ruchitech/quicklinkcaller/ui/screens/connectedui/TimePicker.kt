package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ruchitech.quicklinkcaller.helper.formatTimestampToDate
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.TextFieldBg
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.wheelview.SelectorOptions
import com.ruchitech.quicklinkcaller.wheelview.WheelView
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
private fun TimePicker(selectedTime: (hour: String?, minute: String?, seconds: String?, mode: String?, date: String?) -> Unit) {

    var hour by remember {
        mutableStateOf<String?>(null)
    }
    var minute by remember {
        mutableStateOf<String?>(null)
    }
    var seconds by remember {
        mutableStateOf<String?>(null)
    }
    var mode by remember {
        mutableStateOf<String?>(null)
    }
    var isTodaySelected by remember {
        mutableStateOf(true)
    }

    var isDateSelected by remember {
        mutableStateOf(false)
    }
    var date by remember {
        mutableStateOf<String?>(null)
    }
    var showDatePicker by remember {
        mutableStateOf(false)
    }
    val currentDateTineMap by remember {
        mutableStateOf(parseCurrentDateTime())
    }
    var isDateupdate by remember {
        mutableStateOf(false)
    }
    if (!isDateupdate) {
        hour = currentDateTineMap["hour"]
        minute = currentDateTineMap["minute"]
        seconds = currentDateTineMap["second"]
        mode = currentDateTineMap["mode"]
        date = currentDateTineMap["date"]
    }

    Log.e("kjiuhgfd", "TimePicker: $hour  $isDateupdate")

    DisposableEffect(true) {
        isDateupdate = true
        onDispose { }
    }

    if (showDatePicker) {
        val selectedDate =
            System.currentTimeMillis()
        DatePicker(selectedDate, onCancel = {
            showDatePicker = false
        }, onDateSelected = {
            Log.e("jnhbgf", "TimePicker: $hour")
            showDatePicker = false
            isDateSelected = true
            date = formatTimestampToDate(it)
            selectedTime(hour, minute, seconds, mode, date)
        })
    }
    Column {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ), elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface)
                    .padding(
                        vertical = 0.dp,
                        horizontal = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .height(35.dp)
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isTodaySelected = true
                            isDateSelected = false
                            date = getDate(true)
                            selectedTime(hour, minute, seconds, mode, date)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTodaySelected && !isDateSelected) Color(
                                0x806C63FF
                            ) else TextFieldBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7F),
                    ) {
                        Text(
                            text = "Today",
                            fontFamily = montserrat_semibold,
                            fontSize = 10.sp.nonScaledSp,
                            color = Color(0xFF1F41BB)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            isTodaySelected = false
                            isDateSelected = false
                            date = getDate(false)
                            selectedTime(hour, minute, seconds, mode, date)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isTodaySelected && !isDateSelected) Color(
                                0x806C63FF
                            ) else TextFieldBg
                        ), modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7F)
                    ) {
                        Text(
                            text = "Tomorrow",
                            fontFamily = montserrat_semibold,
                            fontSize = 10.sp.nonScaledSp,
                            color = Color(0xFF1F41BB)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .weight(0.2F)
                        .clickable {
                            showDatePicker = true
                        }
                )

            }

            Box(modifier = Modifier.background(NavySurface)) {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelView(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 0.dp),
                        itemCount = 12,
                        selection = hour?.toInt()?.minus(1) ?: 0,
                        onFocusItem = {
                            val value = if (it.plus(
                                    1
                                ) <= 9
                            ) "0${
                                it.plus(1)
                            }" else "${it.plus(1)}"
                            hour = value
                            Log.e("lmkjnhygt", "focused: $value")
                            selectedTime(hour, minute, seconds, mode, date)
                        },
                        rowOffset = 1,
                        itemSize = DpSize(0.dp, 35.dp),
                        selectorOption = SelectorOptions(
                            enabled = true,
                        )
                    ) {
                        val value = if (it.plus(
                                1
                            ) <= 9
                        ) "0${
                            it.plus(1)
                        }" else "${it.plus(1)}"
                        Text(
                            text = value,
                            color = TextColor,
                            fontSize = 18.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )

                        Log.e("lmkjnhygt", "TimePicker: $value")
                    }

                    Text(
                        text = ":",
                        fontSize = 20.sp.nonScaledSp,
                        modifier = Modifier,
                        color = TextColor,
                        fontWeight = FontWeight.ExtraBold
                    )

                    WheelView(
                        modifier = Modifier.weight(1f),
                        itemCount = 60,
                        selection = minute?.toInt()?.minus(1) ?: 0,
                        rowOffset = 1,
                        onFocusItem = {
                            val value =
                                if (it == 59) "00" else if (it.plus(
                                        1
                                    ) <= 9
                                ) "0${
                                    it.plus(1)
                                }" else "${it.plus(1)}"
                            minute = value
                            selectedTime(hour, minute, seconds, mode, date)
                        },
                        itemSize = DpSize(0.dp, 35.dp),
                        selectorOption = SelectorOptions(
                            enabled = true
                        )
                    ) {
                        val value =
                            if (it == 59) "00" else if (it.plus(
                                    1
                                ) <= 9
                            ) "0${
                                it.plus(1)
                            }" else "${it.plus(1)}"
                        Text(
                            text = value,
                            color = TextColor,
                            fontSize = 18.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }
                    Text(
                        text = ":",
                        fontSize = 20.sp.nonScaledSp,
                        modifier = Modifier,
                        color = TextColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    /*
                                        WheelView(
                                            modifier = Modifier.weight(1f),
                                            itemCount = 60,
                                            selection = seconds?.toInt()?.minus(1) ?: 0,
                                            rowOffset = 1,
                                            onFocusItem = {
                                                val value =
                                                    if (it == 59) "00" else if (it.plus(
                                                            1
                                                        ) <= 9
                                                    ) "0${
                                                        it.plus(1)
                                                    }" else "${it.plus(1)}"
                                                seconds = value
                                                selectedTime(hour, minute, seconds, mode, date)
                                            },
                                            itemSize = DpSize(0.dp, 35.dp),
                                            selectorOption = SelectorOptions(
                                                enabled = true
                                            )
                                        ) {
                                            val value =
                                                if (it == 59) "00" else if (it.plus(
                                                        1
                                                    ) <= 9
                                                ) "0${
                                                    it.plus(1)
                                                }" else "${it.plus(1)}"
                                            Text(
                                                text = value,
                                                color = TextColor,
                                                fontSize = 18.sp.nonScaledSp,
                                                fontFamily = montserrat_semibold
                                            )
                                        }
                    */
                    Text(
                        text = ":",
                        fontSize = 20.sp.nonScaledSp,
                        modifier = Modifier,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    WheelView(
                        modifier = Modifier.weight(1f),
                        itemCount = 2,
                        rowOffset = 1,
                        isEndless = false,
                        selection = if (mode == "AM") 0 else 1,
                        onFocusItem = {
                            val value = if (it == 0) "AM" else "PM"
                            mode = value
                            selectedTime(hour, minute, seconds, mode, date)
                        },
                        itemSize = DpSize(0.dp, 35.dp),
                        selectorOption = SelectorOptions(
                            enabled = true,
                            selectEffectEnabled = true
                        )
                    ) {
                        Text(
                            text = if (it == 0) "AM" else "PM",
                            color = TextColor,
                            fontSize = 16.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }

                }




                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .background(NavySurface)
                        .align(Alignment.BottomCenter)
                )
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimePickerPopup(
    selectedTime: (hour: String?, minute: String?, seconds: String?, mode: String?, date: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPickerOpen by remember {
        mutableStateOf(false)
    }

    var hour by remember {
        mutableStateOf<String?>(null)
    }
    var minute by remember {
        mutableStateOf<String?>(null)
    }
    var seconds by remember {
        mutableStateOf<String?>(null)
    }
    var mode by remember {
        mutableStateOf<String?>(null)
    }
    var isTodaySelected by remember {
        mutableStateOf(true)
    }
    var date by remember {
        mutableStateOf<String?>(null)
    }

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopRight,
        arrowShape = ArrowShape.HalfTriangle,
        arrowOffsetY = 25.dp,
        arrowOffsetX = (-18).dp,
        cornerRadius = 8.dp
    )
    val interactionSource =
        remember { MutableInteractionSource() }
    Popup(
        onDismissRequest = {
            onDismiss()
        },
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, (5).dp.value.roundToInt()),
        properties = PopupProperties(
            clippingEnabled = false,
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    ) {
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 0.dp, end = 10.dp)
                .background(Color.Transparent)
                .clickable(indication = null, interactionSource = interactionSource) { onDismiss() }
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .bubble(
                        bubbleState = bubbleState,
                        color = Color(0xFFFFF2E0),
                    )
                    .fillMaxWidth()
                    .background(Color(0xFFFFF2E0))
                    .padding(10.dp)
            ) {

                Card(
                    modifier = Modifier,
                    border = BorderStroke(
                        0.5.dp,
                        color = Color(0xFFD8D9DB)
                    ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(3.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Text(
                        text = "REMINDER",
                        color = Orange,
                        fontFamily = montserrat_semibold,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 10.sp.nonScaledSp
                    )
                }
                Spacer(modifier = Modifier.height(0.dp))
            /*    Card(
                    onClick = { *//*isPickerOpen = !isPickerOpen*//* },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Time",
                            color = Color.Gray,
                            fontSize = 14.sp.nonScaledSp,
                            fontFamily = montserrat_semibold,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                        Icon(
                            imageVector = if (!isPickerOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }*/
                Spacer(modifier = Modifier.height(10.dp))
                //      if (isPickerOpen) {
                TimePicker { hour1, minute1, seconds1, mode1, date1 ->
                    hour = hour1
                    minute = minute1
                    seconds = seconds1
                    mode = mode1
                    date = date1
                }
                //     }
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(
                            0.5.dp,
                            color = Color.Red
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "CANCEL",
                            color = Color.Red,
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // Get current time in milliseconds

                    Button(
                        onClick = {
                            val currentTimeMillis = System.currentTimeMillis()
                            val minimumTimeMillis =
                                currentTimeMillis + 60000
                            val inMillies =
                                convertTimeStringToMillis("$hour:$minute:$seconds $mode $date")
                            val isValid = inMillies >= minimumTimeMillis
                            if (isValid) {
                                selectedTime(hour, minute, seconds, mode, date)
                                isPickerOpen = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        modifier = Modifier
                            .height(30.dp)
                            .padding(horizontal = 0.dp),
                        border = BorderStroke(
                            0.5.dp,
                            color = ThemePurple
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "SET REMINDER",
                            color = ThemePurple,
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }
                }
            }
        }
    }

}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimePickerPopup2(
    selectedTime: (hour: String?, minute: String?, seconds: String?, mode: String?, date: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPickerOpen by remember {
        mutableStateOf(false)
    }

    var hour by remember {
        mutableStateOf<String?>(null)
    }
    var minute by remember {
        mutableStateOf<String?>(null)
    }
    var seconds by remember {
        mutableStateOf<String?>(null)
    }
    var mode by remember {
        mutableStateOf<String?>(null)
    }
    var isTodaySelected by remember {
        mutableStateOf(true)
    }
    var date by remember {
        mutableStateOf<String?>(null)
    }

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopRight,
        arrowShape = ArrowShape.HalfTriangle,
        arrowOffsetY = 25.dp,
        arrowOffsetX = (-18).dp,
        cornerRadius = 8.dp
    )
    val interactionSource =
        remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 0.dp, end = 10.dp)
                .background(Color.Transparent)
                .clickable(indication = null, interactionSource = interactionSource) { onDismiss() }
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF2E0))
                    .padding(10.dp)
            ) {

                Card(
                    modifier = Modifier,
                    border = BorderStroke(
                        0.5.dp,
                        color = Color(0xFFD8D9DB)
                    ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(3.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Text(
                        text = "REMINDER",
                        color = Orange,
                        fontFamily = montserrat_semibold,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 10.sp.nonScaledSp
                    )
                }
                Spacer(modifier = Modifier.height(0.dp))
                /*    Card(
                        onClick = { *//*isPickerOpen = !isPickerOpen*//* },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Time",
                            color = Color.Gray,
                            fontSize = 14.sp.nonScaledSp,
                            fontFamily = montserrat_semibold,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                        Icon(
                            imageVector = if (!isPickerOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }*/
                Spacer(modifier = Modifier.height(10.dp))
                //      if (isPickerOpen) {
                TimePicker { hour1, minute1, seconds1, mode1, date1 ->
                    hour = hour1
                    minute = minute1
                    seconds = seconds1
                    mode = mode1
                    date = date1
                }
                //     }
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(
                            0.5.dp,
                            color = Color.Red
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "CANCEL",
                            color = Color.Red,
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // Get current time in milliseconds

                    Button(
                        onClick = {
                            val currentTimeMillis = System.currentTimeMillis()
                            val minimumTimeMillis =
                                currentTimeMillis + 60000
                            val inMillies =
                                convertTimeStringToMillis("$hour:$minute:$seconds $mode $date")
                            val isValid = inMillies >= minimumTimeMillis
                            if (isValid) {
                                selectedTime(hour, minute, seconds, mode, date)
                                isPickerOpen = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        modifier = Modifier
                            .height(30.dp)
                            .padding(horizontal = 0.dp),
                        border = BorderStroke(
                            0.5.dp,
                            color = ThemePurple
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "SET REMINDER",
                            color = ThemePurple,
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    }
                }
            }
        }
    }

}



fun convertTimeStringToMillis(timeString: String): Long {
    val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())
    val date = dateFormat.parse(timeString)
    return date?.time ?: 0
}

fun parseTimeString(timeString: String): Map<String, String> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("hh:mm:ss a dd/MM/yyyy", Locale.getDefault())
    calendar.time = dateFormat.parse(timeString)

    val hour = calendar.get(Calendar.HOUR).toString()
    val minute = calendar.get(Calendar.MINUTE).toString()
    val second = calendar.get(Calendar.SECOND).toString()
    val mode = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

    return mapOf(
        "hour" to hour,
        "minute" to minute,
        "second" to second,
        "mode" to mode,
        "date" to date
    )
}

fun parseCurrentDateTime(): Map<String, String> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())

    val hour = calendar.get(Calendar.HOUR).toString()
    val minute = calendar.get(Calendar.MINUTE).toString()
    val second = calendar.get(Calendar.SECOND).toString()
    val mode = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

    return mapOf(
        "hour" to hour,
        "minute" to minute,
        "second" to second,
        "mode" to mode,
        "date" to date
    )
}

fun getDate(isCurrentDate: Boolean): String {
    val calendar = Calendar.getInstance()
    if (!isCurrentDate) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}


fun main() {
    val parsedDateTime = parseCurrentDateTime()
    println("Hour: ${parsedDateTime["hour"]}")
    println("Minute: ${parsedDateTime["minute"]}")
    println("Second: ${parsedDateTime["second"]}")
    println("Mode: ${parsedDateTime["mode"]}")
    println("Date: ${parsedDateTime["date"]}")
}