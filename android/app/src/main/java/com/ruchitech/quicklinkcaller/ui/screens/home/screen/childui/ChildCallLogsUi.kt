package com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui

import android.os.Handler
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.defaultdialer.phonecallui.PhoneCallService
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.helper.formatDuration
import com.ruchitech.quicklinkcaller.helper.formatMilliSecondsToDateTime
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.test1.data.ContactData
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.HintedTextField
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TimePickerPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.LoaderItem
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.ChildCallLogVm
import com.ruchitech.quicklinkcaller.ui.theme.DarkGray
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CallLogCard(log: ChildCallLogVm.CallTypeCountDuration) {
    val bgColor = when (log.type) {
        CallType.MISSED -> Color(0xFFFFEBEE)
        CallType.INCOMING -> Color(0xFFE3F2FD)
        CallType.OUTGOING -> Color(0xFFE8F5E9)
        else -> Color(0xFFF5F5F5)
    }
    val textColor = when (log.type) {
        CallType.MISSED -> Color(0xFFD32F2F)
        CallType.INCOMING -> Color(0xFF1976D2)
        CallType.OUTGOING -> Color(0xFF388E3C)
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = log.type.name.capitalize(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${log.typeCount} calls",
                style = MaterialTheme.typography.bodySmall.copy(color = textColor.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatDuration(log.totalDuration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun CallLogGrid(callLogs: List<ChildCallLogVm.CallTypeCountDuration>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Defines a grid with 2 columns
        contentPadding = PaddingValues(5.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(callLogs) { log ->
            CallLogCard(log)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTopBar(
    name: String?,
    data: CallLogDetails?, // your data class with countryCode and number
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background( MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)).padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        // Back arrow
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Circular avatar with first letter of name
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Orange.copy(alpha = 0.9F),
                    shape = CircleShape
                )
        ) {
            Text(
                text = name?.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontSize = 26.sp.nonScaledSp,
                style = normalGoogleSansStyle
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and number
        Column {
            Text(
                text = name ?: "Unknown",
                fontSize = 20.sp.nonScaledSp,
                style = normalGoogleSansStyle
            )

            if (!data?.countryCode.isNullOrEmpty()) {
                Text(
                    text = "+${data?.countryCode} ${data?.number ?: ""}",
                    fontSize = 16.sp.nonScaledSp,
                    style = normalGoogleSansStyle
                )
            } else {
                Text(
                    text = data?.number ?: "",
                    fontSize = 16.sp.nonScaledSp,
                    style = normalGoogleSansStyle
                )
            }
        }
    }

}



@Composable
fun ChildCallLogsUi(viewModel: ChildCallLogVm) {
    val callLogs by viewModel.callLogsData.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    val name by viewModel.name.collectAsState()
    val data = callLogs.firstOrNull()
    val callLogsSum by viewModel.callLogData.collectAsState()

    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }

    var isAlarm by remember {
        mutableStateOf(false)
    }

    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    if (viewModel.showReminderUi.value) {
        /*
                ReminderUi(reminder, onReminder = { hour, minutes, date ->
                    viewModel.showReminderUi.value = false
                    val actualDate =
                        if (date == "Today") formatTimestampToDate(System.currentTimeMillis()) else date
                    viewModel.setAlarm(
                        hour, minutes, actualDate,
                        viewModel.callLogForReminder.value?.callerId ?: "",
                        viewModel.callLogForReminder.value?.id
                    )
                }) {
                    viewModel.showReminderUi.value = false
                }
        */
    }



    if (showAddNoteDialog) {
        /*
                AddNoteDialog(viewModel, noteText.value, onDismiss = {
                    noteText.value = ""
                    showAddNoteDialog = false
                }) { newNote ->
                    viewModel.isNoteFieldOpen.value = true
                    val tempList = callLogs.toMutableList()
                    val cl = callLogsWithDetails.value?.copy(callNote = newNote)
                    tempList[indexClicked.intValue] =
                        cl?.let { callLogsWithDetails.value?.copy(callNote = it.callNote) }!!
                    viewModel.refreshPreviousScreen(tempList[indexClicked.intValue])
                    viewModel.updateLog(tempList)
                    viewModel.insertNoteOnCallLog(newNote, noteID.longValue)
                    showAddNoteDialog = false
                }
        */
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimary)
    ) {
        Column {
            ContactTopBar(name = name,data) {
                viewModel.navigateUp()
            }
/*
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(start = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Details", fontSize = 16.sp.nonScaledSp)
                }
                Divider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
                */
/*  Spacer(modifier = Modifier.height(10.dp))
                  CircularProfileImage()*//*

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = name ?: "Unknown",
                    fontSize = 16.sp.nonScaledSp,
                    fontFamily = montserrat_medium
                )
                Row {
                    if (!data?.countryCode.isNullOrEmpty()) {

                        Text(
                            text = "+${data?.countryCode} ",
                            fontSize = 13.sp.nonScaledSp,
                            fontFamily = montserrat
                        )

                    }
                    Text(
                        text = data?.number ?: "",
                        fontSize = 13.sp.nonScaledSp,
                        fontFamily = montserrat
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
*/

            CallLogGrid(callLogsSum)
            LeadActionBar(viewModel)
            LazyColumn(state = state) {
                itemsIndexed(callLogs) { index, callLog ->
                    CallLog(callLog, onAddNote = { log, newNote ->
                        showAddNoteDialog = true
                        viewModel.isNoteFieldOpen.value = true
                        val tempList = callLogs.toMutableList()
                        val cl = log.copy(callNote = newNote)
                        tempList[index] =
                            cl.let { log.copy(callNote = it.callNote) }
                        viewModel.refreshPreviousScreen(tempList[index])
                        viewModel.updateLog(tempList)
                        viewModel.insertNoteOnCallLog(newNote, log.id)
                        showAddNoteDialog = false
                    }, onAddAlarm = {
                        //   viewModel.onAddNewAlarm(callLog)
                    }, onFocus = {
                        val currentScroll = state.firstVisibleItemIndex
                        val scrollPosition = currentScroll + index
                        scope.launch {
                            state.animateScrollToItem(
                                index, scrollOffset = 1
                            )
                        }
                        /*               if (it) {
                                           viewModel.openKeyboardWithoutFocus()
                                       } else {
                                           viewModel.hideKeyboard()
                                       }*/
                    }, viewModel = viewModel, onTasksChange = { tasks ->
                        showAddNoteDialog = true
                        viewModel.updateTasks(tasks.toMutableList())
                        viewModel.isNoteFieldOpen.value = true
                        val tempList = callLogs.toMutableList()
                        val cl = callLog.copy(tasks = tasks)
                        tempList[index] =
                            cl.let { callLog.copy(tasks = tasks) }
                        viewModel.updateLog(tempList)
                        viewModel.insertTasksOnChildLogs(callLog.id)
                        viewModel.refreshPreviousScreen(tempList[index])
                        showAddNoteDialog = false
                    })
                    if (index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                        viewModel.loadMoreData()
                    }
                    // Show loader if more data is being loaded
                    if (viewModel.isLoading.collectAsState().value && index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                        LoaderItem()
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CallLog(
    log: CallLogDetails,
    onAddNote: (callLog: CallLogDetails, newNote: String) -> Unit,
    onAddAlarm: () -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    viewModel: ChildCallLogVm,
    onTasksChange: (newTaskList: MutableList<Tasks>) -> Unit,
) {
    val callType = when (log.type) {
        CallType.INCOMING -> "Incoming call"
        CallType.OUTGOING -> "Outgoing call"
        CallType.MISSED -> "Missed call"
        CallType.LOADING -> ""
    }

    var isTaskPopupOpen by remember {
        mutableStateOf(false)
    }

    var isNotePopupOpen by remember {
        mutableStateOf(false)
    }

    var isAlarm by remember {
        mutableStateOf(false)
    }
    val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5F)
                ) {
                    when (log.type) {
                        CallType.OUTGOING -> {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(130f)
                                    //.rotate( if (callLog.type == CallType.OUTGOING) -180F else if (callLog.type== CallType.INCOMING) 0F else 0F)
                                    .padding(top = 0.dp)
                                    .scale(1F),
                                tint = dimBlack,
                            )

                        }

                        CallType.INCOMING -> {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(-45F)
                                    //.rotate( if (callLog.type == CallType.OUTGOING) -180F else if (callLog.type== CallType.INCOMING) 0F else 0F)
                                    .padding()
                                    .scale(1F),
                                tint = dimBlack,
                            )
                        }

                        CallType.MISSED -> {
                            Icon(
                                painter = painterResource(R.drawable.baseline_call_missed_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    //.rotate( if (callLog.type == CallType.OUTGOING) -180F else if (callLog.type== CallType.INCOMING) 0F else 0F)
                                    .padding()
                                    .scale(1F),
                                tint = Color.Red,
                            )
                        }

                        else -> {
                            Icon(
                                painter = painterResource(R.drawable.ic_loading),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(-45F)
                                    //.rotate( if (callLog.type == CallType.OUTGOING) -180F else if (callLog.type== CallType.INCOMING) 0F else 0F)
                                    .padding()
                                    .scale(1F),
                            )

                        }
                    }
                    Column {
                        Text(
                            text = callType,
                            fontSize = 14.sp.nonScaledSp,
                            style = normalGoogleSansStyle,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Text(
                            text = formatTimeAgo(log.date),
                            style = normalGoogleSansStyle.copy(
                                fontSize = 14.sp.nonScaledSp,
                                textAlign = TextAlign.Start,
                                        color = if (log.type == CallType.MISSED) Color.Red else  dimBlack.copy(alpha = 0.8F)
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                Text(
                    text = formatDuration(log.duration),
                    style = normalGoogleSansStyle.copy(
                        fontSize = 14.sp.nonScaledSp,
                        textAlign = TextAlign.Center,
                        color = dimBlack.copy(alpha = 0.9F)
                    ),
                    modifier = Modifier
                        .weight(0.5F)
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .weight(0.6F)
                        .fillMaxWidth()
                        .padding(end = 0.dp)
                ) {

                    Box {

                        IconButton(onClick = {
                            viewModel.onAddNewAlarm(log) {
                                isAlarm = true
                            }
                        }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.alarm),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(1.dp),
                                tint = dimBlack
                            )

                        }
                        if (isAlarm) {
                            TimePickerPopup(
                                onDismiss = { isAlarm = false },
                                selectedTime = { hour, minute, seconds, mode, date ->
                                    val mergeStr = "$hour:$minute:$seconds $mode $date"
                                    if (date != null) {
                                        viewModel.setAlarm(
                                            mergeStr,
                                            date,
                                            viewModel.callLogForReminder.value?.callerId ?: "",
                                            viewModel.callLogForReminder.value?.id
                                        )
                                        Handler().postDelayed({
                                            isAlarm = false
                                        }, 700)
                                    }
                                })
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier) {
                        IconButton(onClick = {
                            isNotePopupOpen = true
                        }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.note_outlined),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(25.dp),
                                tint = DarkGray
                            )

                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier) {
                        IconButton(onClick = {
                            isTaskPopupOpen = true
                        }, modifier = Modifier.size(25.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.tasks_outlined),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp),
                                tint = dimBlack
                            )

                        }
                    }

                }
            }

            if (!log.callNote.isNullOrEmpty()) {
                Text(
                    text = log.callNote ?: "",
                    style = normalGoogleSansStyle.copy(
                        fontSize = 14.sp.nonScaledSp,
                        textAlign = TextAlign.Center,
                        color = dimBlack.copy(alpha = 0.9F)
                    ),
                    color = dimBlack,
                    modifier = Modifier.padding(start = 12.dp, end = 10.dp)
                )
                // Smart Notes Analysis
                if (viewModel.hasGeminiKey) {
                    val noteAnalysisState by viewModel.noteAnalysisState.collectAsState()
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 10.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF6C63FF), modifier = Modifier.size(14.dp))
                        when (val s = noteAnalysisState) {
                            is ChildCallLogVm.NoteAnalysisState.Idle -> TextButton(
                                onClick = { viewModel.analyzeNote(log.callNote ?: "") },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("AI Analyze", fontSize = 11.sp, color = Color(0xFF6C63FF))
                            }
                            is ChildCallLogVm.NoteAnalysisState.Loading -> CircularProgressIndicator(
                                modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Color(0xFF6C63FF)
                            )
                            is ChildCallLogVm.NoteAnalysisState.Success -> {
                                Column {
                                    Text(s.text, fontSize = 12.sp, color = Color(0xFF333333), modifier = Modifier.padding(top = 2.dp))
                                    TextButton(
                                        onClick = { viewModel.resetNoteAnalysis() },
                                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                        modifier = Modifier.height(20.dp)
                                    ) { Text("Clear", fontSize = 10.sp, color = Color.Gray) }
                                }
                            }
                            is ChildCallLogVm.NoteAnalysisState.Error -> TextButton(
                                onClick = { viewModel.resetNoteAnalysis() },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Error — tap to dismiss", fontSize = 11.sp, color = Color(0xFFEF5350))
                            }
                        }
                    }
                }
            }



            if (log.tasks.isNotEmpty()) {
                val task = log.tasks[log.tasks.size - 1]

                if (!task.tasks.isNullOrEmpty()) {
                    Row {
                        Text(
                            text = "${task.tasks}",
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                                .clickable {
                                    isTaskPopupOpen = true
                                },
                            style = TextStyle(textDecoration = if (task.strikeThrough) TextDecoration.LineThrough else TextDecoration.None)
                        )
                        val moreTasks =
                            if (log.tasks.size > 1) "(${log.tasks.size - 1} more)" else ""
                        Text(
                            text = moreTasks,
                            fontSize = 10.sp.nonScaledSp,
                            fontFamily = montserrat_medium,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(end = 10.dp, bottom = 10.dp)
                                .clickable {
                                    isTaskPopupOpen = true
                                }
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(5.dp))

           /* Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp), thickness = 0.5.dp
            )*/
        }
        if (isNotePopupOpen) {
            NotePopup(
                callLog = log,
                onDismiss = {
                    onFocus(false)
                    isNotePopupOpen = false
                },
                onSave = {
                    isNotePopupOpen = false
                    onAddNote(log, it)
                },
                onFocus = { onFocus(it) },
                yourBringIntoViewRequester,
                arrowAlignment = ArrowAlignment.TopRight
            )
        }

        if (isTaskPopupOpen) {
            // viewModel.updateTasks(log.tasks.toMutableList())
            TasksPopup(viewModel, log, onSave = {
                isTaskPopupOpen = false
                onTasksChange(it)
            }, onCancel = {
                isTaskPopupOpen = false
            }, onDelete = {
                val tempList = viewModel.tasks.value?.toMutableList()
                val temp2 = mutableListOf<Tasks>()
                tempList?.forEach { item ->
                    if (it?.taskId != item?.taskId) {
                        if (item != null) {
                            temp2.add(item)
                        }
                    }
                }
                // tempList?.remove(it)
                log.tasks = temp2.toList()
                viewModel.updateTasks(temp2 as MutableList<Tasks?>)
            })
        }
    }
}


@Composable
fun CircularProfileImage() {
    Surface(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // You can use an Image from a resource, URL, etc.
        // Here, I'm using a placeholder icon.
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = Color.LightGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksItem(
    isChecked: Boolean = false,
    tasks: Tasks?,
    onChecked: (isChecked: Boolean) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    onTaskValueChange: (value: String) -> Unit,
) {
    var taskStr by remember {
        mutableStateOf(tasks?.tasks ?: " ")
    }

    var isCheckedBox by remember {
        mutableStateOf(isChecked)
    }


    Row(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCheckedBox, onCheckedChange = {
                isCheckedBox = it
                onChecked(it)
            })

            HintedTextField(
                value = taskStr,
                onValueChange = {
                    taskStr = it
                    onTaskValueChange(it)
                },
                hint = "Enter a tasks here...",
                onDone = {
                    onDone()
                },
                focusRequester = focusRequester,
                onDelete = {
                    onDelete()
                }, onChecked = isCheckedBox
            )
        }

        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier
                .size(25.dp)
                .padding(2.dp)
                .clickable {
                    onDelete()
                },
            tint = Color.Gray
        )
    }
}

@Composable
private fun TasksPopup(
    viewModel: ChildCallLogVm,
    callLog: CallLogDetails,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit,
    onDelete: (task: Tasks?) -> Unit,
) {

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopRight,
        arrowOffsetY = 15.dp,
        arrowOffsetX = (-15).dp,
        arrowShape = ArrowShape.HalfTriangle,
        cornerRadius = 8.dp
    )

    var data by remember {
        mutableStateOf(callLog.tasks)
    }

    LaunchedEffect(true) {
        data = callLog.tasks
    }

    if (data.none { !it.isDelete }) {
        LaunchedEffect(true) {
            delay(100)
            val tempData = data.toMutableList()
            tempData.add(
                Tasks(
                    callLogId = callLog.id,
                    taskId = generateUniqueContactUUID(
                        Math
                            .random()
                            .toString()
                    ),
                    "",
                    Date().time,
                    false
                )
            )
            //  viewModel.updateTasks(tempData)
            data = tempData
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 5.dp, start = 0.dp, end = 10.dp)
            .background(Color.Transparent)
            .clickable(enabled = false) { }
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
            val focusRequesters = remember { mutableStateMapOf<Tasks, FocusRequester>() }
            LazyColumn(modifier = Modifier.height((50.dp * (data.filter { !it.isDelete }.size)))) {
                itemsIndexed(data) { index, s ->
                    val focusRequester = remember { FocusRequester() }
                    focusRequesters[s] = focusRequester
                    if (!s.isDelete) {
                        TasksItem(isChecked = s.strikeThrough, tasks = s, onChecked = {
                            val tempList = data.toMutableList()
                            tempList[index] = s.copy(strikeThrough = it)
                            data = tempList
                        }, onDelete = {
                            s.isDelete = true
                            val temp = data.toMutableList()
                            temp[index] = s.copy(tasks = "${s.tasks}9131414139", isDelete = true)
                            data = temp
                            val lastUndeletedTaskIndex = data.indexOfLast { !it.isDelete }
                            if (lastUndeletedTaskIndex != -1) {
                                val item = data[lastUndeletedTaskIndex]
                                focusRequesters[item]?.requestFocus()
                            }
                        }, onDone = {
                            focusRequesters.clear()
                            val tempData = data.toMutableList()
                            tempData.add(
                                Tasks(
                                    callLogId = callLog.id,
                                    taskId = generateUniqueContactUUID(Math.random().toString()),
                                    "",
                                    Date().time,
                                    false
                                )
                            )
                            // viewModel.updateTasks(tempData)
                            data = tempData
                        }, focusRequester = focusRequester, onTaskValueChange = {
                            val tempList = data.toMutableList()
                            tempList[index] = s.copy(tasks = it)
                            data = tempList
                        })
                    }

                    if (index == data.size.minus(1)) {
                        LaunchedEffect(Unit) {
                            delay(100)
                            val lastUndeletedTaskIndex = data.indexOfLast { !it.isDelete }
                            val item = data[lastUndeletedTaskIndex]
                            focusRequesters[item]?.requestFocus()
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .clickable {
                        val tempData = data.toMutableList()
                        tempData.add(
                            Tasks(
                                callLogId = callLog.id,
                                taskId = generateUniqueContactUUID(
                                    Math
                                        .random()
                                        .toString()
                                ),
                                "",
                                Date().time,
                                false
                            )
                        )
                        data = tempData
                        // viewModel.updateTasks(tempData)
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.drag),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color(0xFFFFF2E0)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "List item")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        /*                      val tempList = arrayListOf<Tasks>()
                                              data.forEach {
                                                  if (it.isDelete) {
                                                      it.isDelete = false
                                                      it.tasks = it.tasks?.split("9131414139")?.get(0)
                                                  }

                                                  if (!it.tasks.isNullOrEmpty()) {
                                                      tempList.add(it)
                                                  }

                                              }
                                              callLog.tasks = tempList
                                              data = tempList*/
                        onCancel()
                    },
                    modifier = Modifier.height(30.dp),
                    border = BorderStroke(0.5.dp, color = Color.Red),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = "Cancel", color = Color.Red, fontSize = 10.sp.nonScaledSp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        val tempList = arrayListOf<Tasks>()
                        data.forEach {
                            if (!it.tasks.isNullOrEmpty() && !it.isDelete) {
                                tempList.add(it)
                            }
                        }
                        onSave(tempList)
                    },
                    modifier = Modifier.height(30.dp),
                    border = BorderStroke(0.5.dp, color = ThemePurple),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(text = "Save", color = ThemePurple, fontSize = 10.sp.nonScaledSp)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))

        }

    }
}

@Composable
private fun LeadActionBar(viewModel: ChildCallLogVm) {
    val leadExists by viewModel.leadExists.collectAsState()
    val isPersonal = viewModel.isPersonal

    androidx.compose.animation.AnimatedVisibility(
        visible = leadExists != null,
        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isPersonal) Color(0xFFFFF8E1)
                    else if (leadExists == true) Color(0xFFF0F9F0)
                    else Color(0xFFF3F4F6)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when {
                isPersonal -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Block, null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                        Text("Personal number", fontSize = 12.sp, color = Color(0xFFD97706), fontFamily = montserrat_medium)
                    }
                    TextButton(
                        onClick = { viewModel.unmarkAsPersonal() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Remove tag", fontSize = 11.sp, color = Color(0xFF6B7280))
                    }
                }
                leadExists == true -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        Text("Saved as lead", fontSize = 12.sp, color = Color(0xFF2E7D32), fontFamily = montserrat_medium)
                    }
                    TextButton(
                        onClick = { viewModel.markAsPersonal() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Mark personal", fontSize = 11.sp, color = Color(0xFF6B7280))
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.convertToLead() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ThemePurple),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.StarBorder, null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Convert to Lead", fontSize = 11.sp)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.markAsPersonal() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Personal", fontSize = 11.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun NotePopup(
    callLog: CallLogDetails,
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    arrowAlignment: ArrowAlignment = ArrowAlignment.TopLeft,
) {
    val bubbleState = rememberBubbleState(
        alignment = arrowAlignment,
        arrowOffsetY = 25.dp,
        arrowOffsetX = (-28).dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var noteText by remember {
        mutableStateOf("")
    }
    var shouldFocus by remember {
        mutableStateOf(true)
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText,
                selection = TextRange(noteText.length)
            )
        )
    }
    Log.e("lkjihg", "NotePopup: 985")

    BackHandler(enabled = shouldFocus) {
        Log.e("lkoijuhy", "NotePopup: 986")
        shouldFocus = false
        focusRequesterForNote.freeFocus()
        onDismiss()
    }

    LaunchedEffect(true) {
        Log.e("lkjihg", "NotePopup: 995")
        noteText = if (!callLog.callNote.isNullOrEmpty()) callLog.callNote!! else ""
        textFieldValueState = TextFieldValue(
            text = noteText,
            selection = TextRange(noteText.length)
        )
        delay(1200)
        focusRequesterForNote.requestFocus()
        /* textFieldValueState =   TextFieldValue(
            text = noteText,
            selection = TextRange()
        ) */

    }

    /*    Popup(
            onDismissRequest = {
                onDismiss()
            },
            alignment = Alignment.TopStart,
            offset = IntOffset(0, (20).dp.value.roundToInt()),
            properties = PopupProperties(
                clippingEnabled = true,
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = true
            )
        ) {*/
    Column(
        modifier = Modifier
            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
            .background(Color.Transparent)
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
                border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(3.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Text(
                    text = "ADD CALL NOTES",
                    color = Orange,
                    fontFamily = montserrat_semibold,
                    modifier = Modifier.padding(6.dp),
                    fontSize = 10.sp.nonScaledSp
                )
            }


            TextField(
                value = textFieldValueState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .focusRequester(focusRequesterForNote)
                    .onFocusChanged {
                        if (shouldFocus) {
                            onFocus(it.isFocused)
                        }
                        /*                            Handler().postDelayed({
                                                        if (!it.isFocused) {
                                                            shouldFocus = false
                                                            // focusRequesterForNote.freeFocus()
                                                            onDismiss()
                                                        }
                                                    }, 500)*/
                    }
                    .onFocusEvent {
                        if (it.isFocused) {
                            coroutineScope.launch {
                                bringIntoViewRequester?.bringIntoView()
                            }
                        }
                    },
                onValueChange = {
                    //noteText = it.text
                    textFieldValueState = it
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color.Black,
                    focusedIndicatorColor = ThemePurple,
                    unfocusedIndicatorColor = Color.LightGray,
                ),
                textStyle = TextStyle(fontFamily = montserrat, fontSize = 12.sp.nonScaledSp),
                placeholder = {
                    Text(
                        text = "Add your text here...",
                        color = Color.LightGray,
                        modifier = Modifier,
                        fontFamily = montserrat,
                        fontSize = 12.sp.nonScaledSp
                    )
                })
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        shouldFocus = false
                        focusRequesterForNote.freeFocus()
                        onDismiss()
                    },
                    modifier = Modifier.height(30.dp),
                    border = BorderStroke(0.5.dp, color = Color.Red),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = "Cancel", color = Color.Red, fontSize = 10.sp.nonScaledSp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        onSave(textFieldValueState.text)
                    },
                    modifier = Modifier.height(30.dp),
                    border = BorderStroke(0.5.dp, color = ThemePurple),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(text = "Save", color = ThemePurple, fontSize = 10.sp.nonScaledSp)
                }
            }
        }
    }
    // }
}
