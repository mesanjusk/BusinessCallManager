package com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ruchitech.quicklinkcaller.helper.isWithin31Days
import com.ruchitech.quicklinkcaller.helper.setEndTimeOfDay
import com.ruchitech.quicklinkcaller.helper.setStartTimeOfDay
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import java.text.SimpleDateFormat
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleDatePickerView(
    onDismissRequest: () -> Unit,
    dates: (date1: String, date2: String) -> Unit
) {
    val currentDate = Calendar.getInstance()
    val maxDate = Calendar.getInstance().apply {
        timeInMillis = currentDate.timeInMillis
    }
    val minDate = Calendar.getInstance().apply {
        add(Calendar.MONTH, -3) // Go back 3 months from current date
    }

    val state = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in minDate.timeInMillis..maxDate.timeInMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in minDate.get(Calendar.YEAR)..maxDate.get(Calendar.YEAR)
            }
        },
        yearRange = minDate.get(Calendar.YEAR)..maxDate.get(Calendar.YEAR),
        initialDisplayMode = DisplayMode.Picker
    )

    // Validate selection doesn't exceed 3 months
    LaunchedEffect(state.selectedStartDateMillis, state.selectedEndDateMillis) {
        state.selectedStartDateMillis?.let { start ->
            state.selectedEndDateMillis?.let { end ->
                val diff = end - start
                val threeMonthsInMillis = 3L * 30 * 24 * 60 * 60 * 1000 // Approximate 3 months
                if (diff > threeMonthsInMillis) {
                    // Reset to valid range if selection exceeds 3 months
                    state.setSelection(
                        startDateMillis = start,
                        endDateMillis = start + threeMonthsInMillis
                    )
                }
            }
        }
    }
    var showRangepicker by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        DateRangePickerSample(state = state, dates = { date1, date2 ->
            dates(date1, date2)
        })
    }
}

fun getFormattedDate(timeInMillis: Long): String {
    val calender = Calendar.getInstance()
    calender.timeInMillis = timeInMillis
    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    return dateFormat.format(calender.timeInMillis)
}

fun dateValidator(): (Long) -> Boolean {
    return { timeInMillis ->
        // Get the selected date
        val selectedDate = Calendar.getInstance()
        selectedDate.timeInMillis = timeInMillis

        // Ensure that the selected date is not in the future
        !selectedDate.after(Calendar.getInstance())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerSample(
    state: DateRangePickerState,
    dates: (date1: String, date2: String) -> Unit
) {
    val context = LocalContext.current
    DateRangePicker(
        state = state,
        modifier = Modifier.background(NavySurface),
        dateFormatter = DatePickerDefaults.dateFormatter("yyyy MM dd", "yyyy MM dd", "yyyy MM dd"),
      //  dateValidator = dateValidator(),
        title = {
            Text(
                text = "Select date range to assign the chart",
                modifier = Modifier
                    .padding(16.dp)
            )
        },
        headline = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    (if (state.selectedStartDateMillis != null) state.selectedStartDateMillis?.let {
                        getFormattedDate(
                            it
                        )
                    } else "Start Date")?.let { Text(text = it) }
                }
                Box(Modifier.weight(1f)) {
                    (if (state.selectedEndDateMillis != null) state.selectedEndDateMillis?.let {
                        getFormattedDate(it)
                    } else "End Date")?.let { Text(text = it) }
                }
                Box(Modifier.weight(0.2f)) {
                    IconButton(onClick = {
                        if (state.selectedStartDateMillis != null && state.selectedEndDateMillis != null) {
                            if (isWithin31Days(
                                    state.selectedStartDateMillis!!,
                                    state.selectedEndDateMillis!!
                                )
                            ) {
                                dates(
                                    setStartTimeOfDay( state.selectedStartDateMillis!!).toString(),
                                    setEndTimeOfDay(state.selectedEndDateMillis!!).toString()
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Max 31 days can be select!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Okk")
                    }
                }

            }
        },
        showModeToggle = true,
        colors = DatePickerDefaults.colors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            headlineContentColor = Color.Black,
            weekdayContentColor = Color.Black,
            subheadContentColor = Color.Black,
            yearContentColor = Color.Green,
            currentYearContentColor = Color.Red,
            selectedYearContainerColor = Color.Red,
            disabledDayContentColor = Color.Gray,
            todayDateBorderColor = Color.Blue,
            dayInSelectionRangeContainerColor = PurpleSolid,
            dayInSelectionRangeContentColor = Color.White,
            selectedDayContainerColor = Color.Black,
            todayContentColor = ThemePurple
        )
    )
}
