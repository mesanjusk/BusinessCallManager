package com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.ui

import android.os.Handler
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.helper.formatReminderTime
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TimePickerPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.TasksItem
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.data.CallLogWithReminder
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.viewmodel.NoteAndReminderVm
import com.ruchitech.quicklinkcaller.ui.theme.DarkGray
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import com.ruchitech.quicklinkcaller.ui.theme.google_sans_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun NoteAndReminderUi(viewModel: NoteAndReminderVm) {
    val notes by viewModel.callLogsData.collectAsState()
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    Box(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                scope.launch {
                    swipeRefreshState.isRefreshing = true
                    //viewModel.
                    delay(1500)
                    viewModel.reInitNotes()
                    swipeRefreshState.isRefreshing = false
                }
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(notes) { index, item ->
                    Note(item, onFocus = {
                        scope.launch {
                            val currentScroll = state.firstVisibleItemIndex
                            val scrollPosition = currentScroll + index
                            state.animateScrollToItem(
                                index,
                                scrollOffset = 1
                            )
                        }
                    }, onAddNote = { callLog, newNote ->
                        item.callLogDetails.callNote = newNote
                        viewModel.insertNoteOnCallLogChild(newNote, callLog.id)
                    },
                        onReminderSet = { time, date ->
                            val data = viewModel.setAlarm(
                                time, date,
                                item.callLogDetails.callerId ?: "",
                                item.callLogDetails.id
                            )
                            item.reminder = data
                        },
                        onTasksChange = { newTaskList ->
                            item.callLogDetails.tasks = newTaskList
                            item.callLogDetails.isSynced = false
                            val tempList = notes.toMutableList()
                            val obj = tempList[index]
                            tempList[index] = obj.copy(
                                callLogDetails = item.callLogDetails.copy(
                                    tasks = newTaskList,
                                    isSynced = false
                                )
                            )
                            viewModel.updateState(tempList)
                            viewModel.updateTasks(newTaskList)
                            viewModel.insertTasksOnChildLogs(item.callLogDetails.id)
                        },
                        onMakeCall = {
                            viewModel.makeCallToNum(item.callLogDetails.number)
                        },
                        onWhatsapp = {
                            viewModel.openWhatsAppByNum(item.callLogDetails.number)
                        },
                        viewModel = viewModel
                    )
                }
                if (notes.isEmpty()) {
                    item {
                        Text(
                            text = "No notes or reminder added yet...",
                            fontSize = 14.sp.nonScaledSp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 25.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Note(
    note: CallLogWithReminder,
    onFocus: (hasFocus: Boolean) -> Unit,
    onAddNote: (callLog: CallLogDetails, newNote: String) -> Unit,
    onReminderSet: (time: String, date: String) -> Unit,
    onMakeCall: () -> Unit,
    onWhatsapp: () -> Unit,
    onTasksChange: (newTaskList: MutableList<Tasks>) -> Unit,
    viewModel: NoteAndReminderVm
) {
    val context = LocalContext.current
    var isNotePopupOpen1 by remember {
        mutableStateOf(false)
    }
    var isNotePopupOpen2 by remember {
        mutableStateOf(false)
    }
    var isAlarm by remember {
        mutableStateOf(false)
    }
    var isAlarm2 by remember {
        mutableStateOf(false)
    }
    var isTaskPopupOpen by remember {
        mutableStateOf(false)
    }
    var isTaskPopupOpen2 by remember {
        mutableStateOf(false)
    }
    val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    if (note.callLogDetails.callNote.isNullOrEmpty()) {
                        IconButton(onClick = {
                            isNotePopupOpen1 = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.note_outlined),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(1.dp),
                                tint = DarkGray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.callLogDetails.tasks.isEmpty()) {
                            IconButton(onClick = {
                                isTaskPopupOpen = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.tasks_outlined),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(1.1.dp),
                                    tint = dimBlack
                                )
                            }
                        }

                        IconButton(onClick = {
                            onMakeCall()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.call_outline),
                                contentDescription = null,
                                tint = dimBlack,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(1.dp)
                                    .rotate(-90F)
                            )
                        }

                        IconButton(onClick = {
                            context.sendTextMessage(note.callLogDetails.number)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.text_msg_outlined),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(1.dp),
                                tint = dimBlack
                            )
                        }

                        if (note.reminder == null || note.reminder?.status == false) {
                            Column {
                                IconButton(onClick = {
                                    isAlarm = true
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.alarm),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .padding(1.2.dp),
                                        tint = dimBlack
                                    )
                                }


                                if (isAlarm) {
                                    TimePickerPopup(
                                        onDismiss = { isAlarm = false },
                                        selectedTime = { hour, minute, seconds, mode, date ->
                                            val mergeStr = "$hour:$minute:$seconds $mode $date"
                                            if (date != null) {
                                                onReminderSet(mergeStr, date)
                                                Handler().postDelayed({
                                                    isAlarm = false
                                                }, 500)
                                            }
                                        })
                                }
                            }
                        }
                        IconButton(onClick = {
                            onWhatsapp()
                        }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(1.dp),
                            )
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Divider(color = Color(0x99E0E2E4))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.callLogDetails.cachedName ?: "Unknown",
                style = normalGoogleSansStyle.copy(
                    fontSize = 16.sp.nonScaledSp,
                    fontFamily = google_sans_medium,
                    color = note.callLogDetails.colorCode,
                    ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .padding(end = 8.dp, start = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.callLogDetails.number,
                    style = normalGoogleSansStyle.copy( color = Color.Gray,
                        fontSize = 14.sp.nonScaledSp,),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(0.8f)
                )
                if (note.reminder != null && note.reminder?.status == true) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                            .clickable {
                                isAlarm2 = true
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.alarm),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(1.2.dp),
                            tint = dimBlack
                        )
                        Text(
                            text = "At ${formatReminderTime(note.reminder?.timeInMillis ?: 0)}",
                            style = normalGoogleSansStyle.copy(fontSize = 14.sp.nonScaledSp),
                            modifier = Modifier.padding(start = 5.dp)
                        )

                        if (isAlarm2) {
                            TimePickerPopup(
                                onDismiss = { isAlarm2 = false },
                                selectedTime = { hour, minute, seconds, mode, date ->
                                    val mergeStr = "$hour:$minute:$seconds $mode $date"
                                    if (date != null) {
                                        onReminderSet(mergeStr, date)
                                        Handler().postDelayed({
                                            isAlarm2 = false
                                        }, 200)
                                    }
                                })
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            if (!note.callLogDetails.callNote.isNullOrEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable {
                                isNotePopupOpen2 = true
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.note_outlined),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(3.75.dp),
                            tint = DarkGray
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = note.callLogDetails.callNote ?: "",
                            style = normalGoogleSansStyle.copy(
                                fontSize = 14.sp.nonScaledSp,
                                color = dimBlack,
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .padding(
                                    start = 10.dp,
                                    end = 25.dp,
                                    top = 7.5.dp,
                                    bottom = 7.5.dp
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isNotePopupOpen2) {
                        NotePopup(
                            callLog = note.callLogDetails,
                            onDismiss = {
                                onFocus(false)
                                isNotePopupOpen2 = false
                            },
                            onSave = {
                                isNotePopupOpen2 = false
                                onAddNote(note.callLogDetails, it)
                            },
                            onFocus = { onFocus(it) },
                            yourBringIntoViewRequester,
                            arrowAlignment = ArrowAlignment.TopLeft,
                            topPadding1 = 0.dp,
                            topPadding2 = 0.dp
                        )
                    }
                }

            }
            if (note.callLogDetails.tasks.isNotEmpty()) {
                val task = note.callLogDetails.tasks[note.callLogDetails.tasks.size - 1]
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable {
                                viewModel.updateTasks(
                                    note.callLogDetails.tasks.toMutableList(),
                                    false
                                )
                                viewModel.orgTaskList = note.callLogDetails.tasks
                                isTaskPopupOpen2 = true
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tasks_outlined),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(3.90.dp),
                            tint = dimBlack
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Row(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "${task.tasks}",
                                fontSize = 14.sp.nonScaledSp,
                                overflow = TextOverflow.Ellipsis,
                                color = dimBlack,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(
                                        start = 10.dp,
                                        end = 5.dp,
                                        top = 7.5.dp,
                                        bottom = 7.5.dp
                                    ),
                                style = TextStyle(textDecoration = if (task.strikeThrough) TextDecoration.LineThrough else TextDecoration.None)
                            )
                            val moreTasks =
                                if (note.callLogDetails.tasks.size > 1) "(${note.callLogDetails.tasks.size - 1} more)" else ""
                            Text(
                                text = moreTasks,
                                fontSize = 14.sp.nonScaledSp,
                                fontFamily = google_sans_medium,
                                color = Color.Gray,
                                modifier = Modifier
                                    .clickable {
                                        isTaskPopupOpen2 = true
                                    }
                            )
                        }
                    }
                    if (isTaskPopupOpen2) {
                        TasksPopup2(viewModel, note.callLogDetails, onSave = {
                            isTaskPopupOpen2 = false
                            onTasksChange(it)
                        }, onCancel = {
                            isTaskPopupOpen2 = false
                        })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

            }
        }

        if (isNotePopupOpen1) {
            NotePopup(
                callLog = note.callLogDetails,
                onDismiss = {
                    onFocus(false)
                    isNotePopupOpen1 = false
                },
                onSave = {
                    isNotePopupOpen1 = false
                    onAddNote(note.callLogDetails, it)
                },
                onFocus = { onFocus(it) },
                yourBringIntoViewRequester,
                arrowAlignment = ArrowAlignment.TopLeft
            )
        }
        if (isTaskPopupOpen) {
            TasksPopup(viewModel, note.callLogDetails, onSave = {
                isTaskPopupOpen = false
                onTasksChange(it)
            }, onCancel = {
                isTaskPopupOpen = false
            })
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotePopup(
    callLog: CallLogDetails,
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    arrowAlignment: ArrowAlignment = ArrowAlignment.TopLeft,
    topPadding1: Dp = 10.dp,
    topPadding2: Dp = 20.dp
) {
    val bubbleState = rememberBubbleState(
        alignment = arrowAlignment,
        arrowOffsetY = 20.dp,
        arrowOffsetX = 10.dp,
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

    BackHandler(enabled = shouldFocus) {
        shouldFocus = false
        focusRequesterForNote.freeFocus()
        onDismiss()
    }

    LaunchedEffect(true) {
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
            .padding(top = topPadding1, start = 10.dp, end = 10.dp)
            .background(Color.Transparent)
    ) {
        Spacer(modifier = Modifier.height(topPadding2))
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

@Composable
private fun TasksPopup(
    viewModel: NoteAndReminderVm,
    callLog: CallLogDetails,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit
) {

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopCenter,
        arrowOffsetY = 25.dp,
        arrowOffsetX = (-20).dp,
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
                        Math.random().toString()
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
            .padding(top = 15.dp, start = 10.dp, end = 10.dp)
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
                            s.strikeThrough = it
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

            /*   data?.forEachIndexed { index, s ->


               }*/

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
private fun TasksPopup2(
    viewModel: NoteAndReminderVm,
    callLog: CallLogDetails,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit
) {

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 5.dp,
        arrowOffsetX = 10.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val data = remember {
        mutableStateOf(listOf<Tasks>())
    }


    LaunchedEffect(true) {
        data.value = callLog.tasks
    }

    if (data.value.none { !it.isDelete }) {
        LaunchedEffect(true) {
            delay(100)
            val tempData = data.value.toMutableList()
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
            data.value = tempData
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 5.dp, start = 0.dp, end = 10.dp)
            .background(Color.Transparent)
            .clickable(enabled = false) { }
    ) {
        Spacer(modifier = Modifier.height(0.dp))
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
            LazyColumn(modifier = Modifier.height((50.dp * (data.value.filter { !it.isDelete }.size)))) {
                itemsIndexed(data.value) { index, s ->
                    val focusRequester = remember { FocusRequester() }
                    focusRequesters[s] = focusRequester
                    if (!s.isDelete) {
                        TasksItem(isChecked = s.strikeThrough, tasks = s, onChecked = {
                            s.strikeThrough = it
                        }, onDelete = {
                            s.isDelete = true
                            val temp = data.value.toMutableList()
                            temp[index] = s.copy(tasks = "${s.tasks}9131414139", isDelete = true)
                            data.value = temp
                            val lastUndeletedTaskIndex = data.value.indexOfLast { !it.isDelete }
                            if (lastUndeletedTaskIndex != -1) {
                                val item = data.value[lastUndeletedTaskIndex]
                                focusRequesters[item]?.requestFocus()
                            }
                        }, onDone = {
                            focusRequesters.clear()
                            val tempData = data.value.toMutableList()
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
                            data.value = tempData
                        }, focusRequester = focusRequester, onTaskValueChange = {
                            //s.tasks = it
                            val tempList = data.value.toMutableList()
                            tempList[index] = s.copy(tasks = it)
                            data.value = tempList
                        })
                    }
                    if (index == data.value.size.minus(1)) {
                        LaunchedEffect(Unit) {
                            delay(100)
                            val lastUndeletedTaskIndex = data.value.indexOfLast { !it.isDelete }
                            val item = data.value[lastUndeletedTaskIndex]
                            focusRequesters[item]?.requestFocus()
                        }
                    }
                }
            }

            /*   data?.forEachIndexed { index, s ->


               }*/

            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .clickable {
                        val tempData = data.value.toMutableList()
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
                        data.value = tempData
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
                        data.value.forEach {
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
