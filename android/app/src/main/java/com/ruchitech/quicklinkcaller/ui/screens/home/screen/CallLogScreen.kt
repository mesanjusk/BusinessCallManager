package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import SaveContactUi
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ruchitech.quicklinkcaller.NotePopup
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.helper.copyToClipboard
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.helper.shareContact
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.test1.CallLogItem
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.DialPadScreen
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.HintedTextField
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TimePickerPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui.SampleDatePickerView
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.theme.DarkGray
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import com.ruchitech.quicklinkcaller.ui.theme.google_sans_medium
import com.ruchitech.quicklinkcaller.ui.theme.google_sans_regular
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.Date
import kotlin.math.roundToInt


data class CallLog(
    val callType: CallType, val name: String, val phoneNumber: String, val time: String,
)

enum class CallType {
    INCOMING, OUTGOING, MISSED, LOADING
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CallLogScreen(viewModel: HomeVm) {
    val callLogs by viewModel.callLogsData.collectAsState()
    val callLogsUi by viewModel.callLogsUi.collectAsState()
    val searchCallLog by viewModel.searchCallLogs.collectAsState()
    val searchCallLogUi by viewModel.searchCallLogsUi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val callLogsWithDetails = remember {
        mutableStateOf<CallLogsWithDetails?>(null)
    }

    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }

    var showDateRangePicker by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    // Remember the state for the SwipeRefresh
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val state = rememberLazyListState()
    LaunchedEffect(state, isLoading, viewModel.isNoteFieldOpen.value) {
        snapshotFlow {
            val lastVisible = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = state.layoutInfo.totalItemsCount
            lastVisible to total
        }.map { (lastVisible, total) ->
            lastVisible >= 0 && total > 0 && lastVisible >= total - 1
        }.distinctUntilChanged().filter { shouldLoadMore ->
            shouldLoadMore &&
                    !isLoading &&
                    !viewModel.isNoteFieldOpen.value
        }.collect {
            viewModel.loadMoreData()
        }
    }
    if (showDateRangePicker) {
        SampleDatePickerView(dates = { date1, date2 ->
            viewModel.fetchCallLogsByDate(date1, date2)
            showDateRangePicker = false
        }, onDismissRequest = {
            showDateRangePicker = false
        })
    }

    /*    if (viewModel.showReminderUi.value) {
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


        }*/

    if (showAddNoteDialog) {

    }

    val context = LocalContext.current
    LocalDensity.current

    var showDialPad by remember { mutableStateOf(false) }
    val dialPadState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(bottomBar = {
        if (!showDialPad) Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp, end = 18.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = {
                    showDialPad = true
                }, modifier = Modifier
                    .size(50.dp) // Increased size for better visibility
                    .clip(RoundedCornerShape(10.dp)) // Makes it a perfect circle
                    .background(PurpleSolid) // Background color
                // .shadow(elevation = 8.dp, shape = CircleShape), // Adds depth
                , colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dialpad),
                    contentDescription = "Dialpad",
                    modifier = Modifier
                        .size(30.dp) // Adjusting icon size
                        .padding(3.dp),
                    tint = Color.White
                )
            }
        } else ModalBottomSheet(onDismissRequest = {
            viewModel.searchCallLogs("")
            showDialPad = false
        }, sheetState = dialPadState) {
            DialPadScreen(onNumberChange = { value ->
                viewModel.searchCallLogs(value)
            }, onActionDial = { number ->
                viewModel.makeCallToNum(number)
            })
        }

    }) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Divider()
            Spacer(modifier = Modifier.height(10.dp))
            SwipeRefresh(state = swipeRefreshState, onRefresh = {
                scope.launch {
                    swipeRefreshState.isRefreshing = true
                    viewModel.updateRecentCallLogs()
                    delay(1500)
                    viewModel.reinitCallLogs()
                    swipeRefreshState.isRefreshing = false
                }
            }) {

                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (searchCallLog.isEmpty() && viewModel.query.value.isEmpty()) {
                        itemsIndexed(
                            callLogsUi,
                            key = { _, callLog -> callLog.model.callLogs.id },
                            contentType = { _, _ -> "call_log_item" }
                        ) { index, callLogUi ->
                            val currentLatest = callLogUi.latest
                            val callLog = callLogs.getOrNull(index) ?: return@itemsIndexed
                            if (currentLatest != null) {
                                CallLogItem(
                                    viewModel,
                                    latestNote = currentLatest,
                                    callLog = currentLatest,
                                    callLog1 = callLog,
                                    displayName = callLogUi.displayName,
                                    formattedTime = callLogUi.formattedTime,
                                    avatarBg = callLogUi.avatarBg,
                                    avatarText = callLogUi.avatarText,
                                    onCallIcon = {
                                        viewModel.makeCallToNum(callLog.callLogs.callerId)
                                    },
                                    onSaveInPhonebook = {
                                        viewModel.saveNumberInPhonebook(
                                            callLog.callLogs.callerId,
                                            currentLatest.cachedName ?: ""
                                        )
                                    },
                                    onAddAlarm = {
                                        //  viewModel.onAddNewAlarm(currentLatest)
                                    },
                                    onWhatsappIcon = {
                                        viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                                    },
                                    onParentClick = {
                                        viewModel.lastClickedCallLog.value = callLog
                                        viewModel.navigateToCallLogDetails(
                                            callLog.callLogs.callerId,
                                            callLog.callLogs.id.toString(),
                                            currentLatest
                                        )
                                    },
                                    onContactSavedClick = { name: String?, number: String? ->
                                        viewModel.checkNumberSavedOrNot(name, number)
                                    },
                                    onAddNote = { data, newText ->
                                        showAddNoteDialog = true
                                        val currentIdx = index
                                        val childIdx = callLog.callLogDetails.indexOf(data)
                                        val tempList = callLogs.toMutableList()
                                        val cl = callLog.callLogDetails.toMutableList()
                                        val newData1 =
                                            cl.getOrNull(childIdx)?.copy(callNote = newText)
                                        newData1?.let { cl[childIdx] = it }
                                        val updatedLog = callLog.copy(callLogDetails = cl)
                                        updatedLog.callLogs.callNote = newText
                                        tempList[currentIdx] = updatedLog
                                        showAddNoteDialog = false
                                        viewModel.updateCallLogById(tempList)
                                        viewModel.insertNoteOnCallLogChild(newText, data.id)
                                    },
                                    onFocus = {
                                        if (it) {
                                            scope.launch {
                                                state.animateScrollToItem(index, scrollOffset = 1)
                                            }
                                        }
                                    },
                                    onShareContact = { name, number ->
                                        context.shareContact(name ?: "", number)
                                    },
                                    onInvoke = {
                                    },
                                    onTasksChange = {
                                        showAddNoteDialog = true
                                        viewModel.updateTasks(it.toMutableList(), true)
                                        val data = currentLatest
                                        val currentIdx = index
                                        val childIdx = callLog.callLogDetails.indexOf(data)
                                        val tempList = callLogs.toMutableList()
                                        val cl = callLog.callLogDetails.toMutableList()
                                        val newData1 =
                                            cl.getOrNull(childIdx)?.copy(tasks = it.toList())
                                        newData1?.let { updated -> cl[childIdx] = updated }
                                        val updatedLog = callLog.copy(callLogDetails = cl)
                                        updatedLog.callLogs.callNote = "${Date().time}"
                                        tempList[currentIdx] = updatedLog
                                        showAddNoteDialog = false
                                        viewModel.updateCallLogById(tempList)
                                        viewModel.insertTasksOnChildLogs(data.id)

                                    })
                            }
                        }
                    } else {
                        if (searchCallLog.isEmpty()) {
                            item {
                                Text(
                                    text = "No record found!",
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 25.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        itemsIndexed(
                            searchCallLogUi,
                            key = { _, callLog -> callLog.model.callLogs.id },
                            contentType = { _, _ -> "call_log_item_search" }
                        ) { index, callLogUi ->
                            val callLog = searchCallLog.getOrNull(index) ?: return@itemsIndexed
                            val currentLatest = callLogUi.latest ?: return@itemsIndexed
                            CallLogItem(
                                viewModel,
                                latestNote = callLog.callLogDetails.filter { !it.callNote.isNullOrEmpty() }
                                    .firstOrNull(),
                                callLog = currentLatest,
                                callLog1 = callLog,
                                displayName = callLogUi.displayName,
                                formattedTime = callLogUi.formattedTime,
                                avatarBg = callLogUi.avatarBg,
                                avatarText = callLogUi.avatarText,
                                onCallIcon = {
                                    viewModel.makeCallToNum(callLog.callLogs.callerId)
                                },
                                onSaveInPhonebook = {
                                    viewModel.saveNumberInPhonebook(
                                        callLog.callLogs.callerId,
                                        currentLatest.cachedName ?: ""
                                    )
                                },
                                onAddAlarm = {
                                    //viewModel.onAddNewAlarm(currentLatest)
                                },
                                onWhatsappIcon = {
                                    viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                                },
                                onParentClick = {
                                    viewModel.navigateToCallLogDetails(
                                        callLog.callLogs.callerId,
                                        callLog.callLogs.id.toString(),
                                        currentLatest
                                    )
                                },
                                onContactSavedClick = { name: String?, number: String? ->
                                    viewModel.checkNumberSavedOrNot(name, number)
                                },
                                onAddNote = { data, newText ->
                                    showAddNoteDialog = true
                                    val currentIdx = index
                                    val childIdx = callLog.callLogDetails.indexOf(data)
                                    callLogsWithDetails.value = callLog
                                    val tempList = callLogs.toMutableList()
                                    val cl =
                                        callLogsWithDetails.value?.callLogDetails?.toMutableList()
                                    val newData1 = cl?.get(childIdx)?.copy(callNote = newText)
                                    newData1?.let { cl.set(childIdx, it) }
                                    val newData = tempList[currentIdx]
                                    if (cl != null) {
                                        newData.callLogDetails = cl
                                    }
                                    newData.callLogs.callNote = newText
                                    tempList[currentIdx] = newData
                                    showAddNoteDialog = false
                                    viewModel.updateCallLogById(tempList)
                                    viewModel.insertNoteOnCallLogChild(newText, data.id)
                                    val tempList2 = searchCallLog.toMutableList()
                                    tempList2[index] = newData
                                    viewModel.updateSearchEditLog(tempList2)
                                },
                                onFocus = {
                                    scope.launch {
                                        state.animateScrollToItem(
                                            index, scrollOffset = 1
                                        )
                                    }
                                    if (it) {
                                        viewModel.openKeyboardWithoutFocus()
                                    } else {
                                        viewModel.hideKeyboard()
                                    }
                                },
                                onShareContact = { name, number ->
                                    context.shareContact(name ?: "", number)
                                },
                                onInvoke = {
                                },
                                onTasksChange = { tasksMutableList ->
                                    showAddNoteDialog = true
                                    viewModel.updateTasks(tasksMutableList.toMutableList(), true)
                                    val data = currentLatest
                                    val currentIdx = index
                                    val childIdx = callLog.callLogDetails.indexOf(data)
                                    callLogsWithDetails.value = callLog
                                    val tempList = callLogs.toMutableList()
                                    val cl =
                                        callLogsWithDetails.value?.callLogDetails?.toMutableList()
                                    val newData1 =
                                        cl?.get(childIdx)?.copy(tasks = tasksMutableList.toList())
                                    newData1?.let { cl.set(childIdx, it) }
                                    val newData = tempList[currentIdx]
                                    if (cl != null) {
                                        newData.callLogDetails = cl
                                    }
                                    newData.callLogs.callNote = "${Date().time}"
                                    tempList[currentIdx] = newData
                                    showAddNoteDialog = false
                                    viewModel.updateCallLogById(tempList)
                                    viewModel.insertTasksOnChildLogs(data.id)
                                    val tempList2 = searchCallLog.toMutableList()
                                    tempList2[index] = newData
                                    viewModel.updateSearchEditLog(tempList2)
                                })
                        }
                    }


                    if (isLoading) {
                        item(key = "loader_item") { LoaderItem() }
                    }/*                   item {
                                               if (addHeight) {
                                                   Spacer(modifier = Modifier.height(500.dp))
                                               }
                                           }*/
                }
            }
        }

    }

}



@Composable
fun LoaderItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            color = Color(0xFFF6F7FB)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = ThemePurple,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Loading older…",
                    style = normalGoogleSansStyle,
                    fontSize = 13.sp.nonScaledSp,
                    color = dimBlack.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun CallLogItem(
    viewModel: HomeVm,
    latestNote: CallLogDetails?,
    callLog: CallLogDetails,
    callLog1: CallLogsWithDetails,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onSaveInPhonebook: () -> Unit,
    onAddAlarm: (mergeDateStr: String) -> Unit,
    onAddNote: (callLog: CallLogDetails, newNote: String) -> Unit,
    onContactSavedClick: (name: String?, number: String?) -> Unit,
    onParentClick: () -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    onShareContact: (name: String?, number: String) -> Unit,
    onInvoke: (a: Boolean) -> Unit,
    onTasksChange: (newTaskList: MutableList<Tasks>) -> Unit,
    displayName: String = callLog.cachedName?.takeIf { it.isNotBlank() && it != "Unknown" } ?: "Unknown",
    formattedTime: String = formatTimeAgo(callLog.date),
    avatarBg: Color = when {
        callLog.colorCode == Orange -> Orange
        displayName == "Unknown" -> if ((callLog.number.hashCode() and 1) == 0) Orange else Color.Black
        else -> Color.Black
    },
    avatarText: Color = if (avatarBg == Color.Black) Color.White else Color.Black,
) {
    var expanded by remember { mutableStateOf(false) }
    var showSaveInappSheet by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val isContactsPopupOpen = remember {
        mutableStateOf(false)
    }
    var isNotePopupOpen1 by remember {
        mutableStateOf(false)
    }
    var isTaskPopupOpen by remember {
        mutableStateOf(false)
    }
    var isTaskPopupOpen2 by remember {
        mutableStateOf(false)
    }
    var isNotePopupOpen2 by remember {
        mutableStateOf(false)
    }
    var isAlarm by remember {
        mutableStateOf(false)
    }
    val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    var isShareContactOpen by remember { mutableStateOf(false) }
    val saveContactSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .padding(if (expanded) 10.dp else 0.dp)
            .background(
                color = if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else Color.Transparent, shape = RoundedCornerShape(12.dp)
            )
            .animateContentSize()
            .clickable(onClick = {
                expanded = !expanded /*onParentClick()*/
            })
    ) {
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(
                        end = 5.dp,
                        bottom = if (callLog.callNote.isNullOrEmpty()) 8.dp else 0.dp,
                        top = 5.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                ) {
                    Text(
                        text = displayName.first().uppercase(),
                        fontSize = 24.sp.nonScaledSp,
                        fontFamily = google_sans_medium,
                        color = avatarText
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1F), verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val isKnownName = displayName != "Unknown"
                    val nameColor = if (isKnownName) callLog.colorCode else dimBlack
                    val displayTitle = if (isKnownName) {
                        displayName
                    } else {
                        "${if (!callLog.countryCode.isNullOrEmpty()) "+${callLog.countryCode}" else ""} ${callLog.number}"
                    }
                    Text(
                        text = displayTitle,
                        style = normalGoogleSansStyle,
                        fontSize = 16.sp.nonScaledSp,
                        overflow = TextOverflow.Ellipsis,
                        color = nameColor,
                        maxLines = 1,
                        lineHeight = 0.sp,
                        modifier = Modifier.padding(end = 8.dp, start = 5.dp)
                    )


                    Row(
                        modifier = Modifier.padding(start = 2.dp, end = 8.dp, top = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (callLog.type) {
                            CallType.OUTGOING -> {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(15.dp)
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
                                        .size(15.dp)
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
                                        .size(15.dp)
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
                                        .size(15.dp)
                                        .rotate(-45F)
                                        //.rotate( if (callLog.type == CallType.OUTGOING) -180F else if (callLog.type== CallType.INCOMING) 0F else 0F)
                                        .padding()
                                        .scale(1F),
                                )

                            }
                        }

                        Text(
                            text = " • ", // middle dot character
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = dimBlack.copy(alpha = 0.4F)
                        )
                        Text(
                            text = formattedTime,
                            overflow = TextOverflow.Ellipsis,
                            style = normalGoogleSansStyle.copy(color = dimBlack.copy(alpha = 0.8F)),
                            fontSize = 14.sp.nonScaledSp,
                            maxLines = 1,
                            modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .scale(1F)
                                .clickable {
                                    context.copyToClipboard(callLog.number)
                                },
                            tint = Color.LightGray,
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .scale(1F)
                                .clickable {
                                    onShareContact(
                                        callLog.cachedName, callLog.number
                                    )
                                },
                            tint = Color.LightGray,
                        )

                    }
                }

                Row(modifier = Modifier.weight(0.35F), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        onCallIcon()
                    }, modifier = Modifier) {
                        Icon(
                            painter = painterResource(id = R.drawable.call_outline),
                            contentDescription = null,
                            tint = dimBlack,
                            modifier = Modifier
                                .size(35.dp)
                                .padding(top = 10.dp)
                                .rotate(-90F)
                        )
                    }
                    IconButton(onClick = { onWhatsappIcon() }, modifier = Modifier) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                                .padding(5.dp)
                        )
                    }
                }

            }
            if (!callLog.callNote.isNullOrEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable { isNotePopupOpen2 = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.note_outlined),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(5.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = callLog.callNote ?: "",
                            fontFamily = google_sans_regular,
                            fontSize = 14.sp.nonScaledSp,
                            overflow = TextOverflow.Ellipsis,
                            color = dimBlack,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()/* .background(
                                     color = Color(0xFFF5F5F5), shape = RoundedCornerShape(5.dp)
                                 )*/.padding(
                                    start = 5.dp, end = 25.dp, top = 7.5.dp, bottom = 7.5.dp
                                )
                        )
                    }
                    ///   Spacer(modifier = Modifier.height(10.dp))

                    if (isNotePopupOpen2) {
                        isTaskPopupOpen = false
                        isTaskPopupOpen2 = false
                        NotePopup2(callLog = callLog, onDismiss = {
                            //onFocus(false)
                            isNotePopupOpen2 = false
                        }, onSave = {
                            isNotePopupOpen2 = false
                            onAddNote(callLog, it)
                        }, onFocus = { onFocus(it) }, yourBringIntoViewRequester)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(color = Color(0x99E0E2E4))
                }
            }

            if (callLog.tasks.isNotEmpty()) {
                val task = callLog.tasks[callLog.tasks.size - 1]
                if (!task.tasks.isNullOrEmpty()) {
                    Column {
                        //  Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clickable {
                                    Log.e("kijuhgyfd", "CallLogItem: ${callLog.tasks.size}")
                                    viewModel.updateTasks(callLog.tasks.toMutableList(), false)
                                    isTaskPopupOpen2 = true
                                }) {
                            Icon(
                                painter = painterResource(id = R.drawable.tasks_outlined),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(5.dp),
                                tint = dimBlack
                            )
                            Spacer(modifier = Modifier.width(5.dp))


                            Row(
                                modifier = Modifier/*   .background(
                                           color = Color(0xFFF5F5F5),
                                           shape = RoundedCornerShape(5.dp)
                                       )*/.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "${task.tasks}",
                                    fontFamily = google_sans_regular,
                                    fontSize = 14.sp.nonScaledSp,
                                    overflow = TextOverflow.Ellipsis,
                                    color = dimBlack,
                                    maxLines = 1,
                                    modifier = Modifier.padding(
                                        start = 0.dp, end = 5.dp, top = 7.5.dp, bottom = 7.5.dp
                                    ),
                                    style = TextStyle(textDecoration = if (task.strikeThrough) TextDecoration.LineThrough else TextDecoration.None)
                                )
                                val moreTasks =
                                    if (callLog.tasks.size > 1) "(${callLog.tasks.size - 1} more)" else ""
                                Text(
                                    text = moreTasks,
                                    fontSize = 14.sp.nonScaledSp,
                                    fontFamily = google_sans_regular,
                                    color = dimBlack,
                                    modifier = Modifier.clickable {
                                        isTaskPopupOpen2 = true
                                    })
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Divider(color = Color(0x99E0E2E4))
                    }


                    if (isTaskPopupOpen2) {

                        TasksPopup2(viewModel, callLog, onSave = {
                            isTaskPopupOpen2 = false
                            onTasksChange(it)
                        }, onCancel = {
                            isTaskPopupOpen2 = false
                        })
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                }

            }/*        if (!expanded) HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color(0x99E0E2E4)
                    )*/
            if (expanded) {/*            Text(
                                text = formatTimeAgo(callLog.date),
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = montserrat,
                                fontSize = 14.sp.nonScaledSp,
                                color = Color.Gray,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)

                            )*/
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(modifier = Modifier.weight(1f)) {
                        IconButton(onClick = {
                            isContactsPopupOpen.value = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.contact_book_filled),
                                contentDescription = null,
                                tint = dimBlack,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(3.50.dp)
                            )
                        }
                        if (isContactsPopupOpen.value) {
                            val buttonDisableNum =
                                if (callLog.colorCode == Orange && callLog.cachedName != "Unknown") 2 else if (callLog.cachedName != "Unknown" && callLog.colorCode != Orange) 1 else 0
                            val bubbleState = rememberBubbleState(
                                alignment = ArrowAlignment.BottomLeft,
                                arrowOffsetY = 1.dp,
                                arrowOffsetX = 25.dp,
                                cornerRadius = 8.dp
                            )
                            Popup(
                                onDismissRequest = { isContactsPopupOpen.value = false },
                                alignment = Alignment.TopStart,
                                offset = IntOffset(0, (-150).dp.value.roundToInt()),
                                properties = PopupProperties(
                                    dismissOnClickOutside = true, dismissOnBackPress = true
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(bottom = 0.dp)
                                        .background(Color.Transparent)
                                        .clickable { isContactsPopupOpen.value = false }) {
                                    Row(
                                        modifier = Modifier
                                            .bubble(
                                                bubbleState = bubbleState,
                                                color = ThemePurple,
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "Primary",
                                            textAlign = TextAlign.Center,
                                            fontFamily = montserrat_semibold,
                                            fontSize = 10.sp.nonScaledSp,
                                            modifier = Modifier
                                                .width(90.dp)
                                                .background(
                                                    if (buttonDisableNum == 1) Color.LightGray else Color.White,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 8.dp)
                                                .clickable(enabled = buttonDisableNum != 1) {
                                                    isContactsPopupOpen.value = false
                                                    onSaveInPhonebook()
                                                },
                                            color = if (buttonDisableNum == 1) Color.Gray else ThemePurple,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Secondary",
                                            textAlign = TextAlign.Center,
                                            fontFamily = montserrat_semibold,
                                            fontSize = 10.sp.nonScaledSp,
                                            modifier = Modifier
                                                .width(90.dp)
                                                .background(
                                                    if (buttonDisableNum == 2) Color.LightGray else Color.White,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 8.dp)
                                                .clickable(enabled = buttonDisableNum != 2) {
                                        isContactsPopupOpen.value = false
                                        showSaveInappSheet = true
                                        },
                                        color = if (buttonDisableNum == 2) Color.Gray else ThemePurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                    Spacer(modifier = Modifier.height(30.dp))
                                }
                            }
                        }
                    }

                    IconButton(onClick = { onParentClick() }, modifier = Modifier.weight(1f)) {
                        Icon(
                            painter = painterResource(id = R.drawable.call_history),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(2.dp),
                            tint = dimBlack
                        )
                    }


                    Column(modifier = Modifier.weight(1f)) {
                        if (callLog.callNote.isNullOrEmpty()) {
                            IconButton(onClick = {
                                viewModel.updateTasks(callLog.tasks.toMutableList(), false)
                                isNotePopupOpen1 = true
                                onInvoke(true)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.note_outlined),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(3.10.dp),
                                    tint = DarkGray
                                )
                            }
                        }
                    }
                    var task: Tasks? = null
                    if (callLog.tasks.isNotEmpty()) {
                        task = callLog.tasks[callLog.tasks.size - 1]
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (callLog.tasks.isEmpty() || task?.tasks.isNullOrEmpty()) {
                            IconButton(onClick = {
                                viewModel.updateTasks(callLog.tasks.toMutableList(), false)
                                isTaskPopupOpen = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.tasks_outlined),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(3.90.dp),
                                    tint = dimBlack
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.contact_search),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(top = 3.dp)
                                .scale(1F)
                                .clickable {
                                    isShareContactOpen = true
                                },
                            tint = dimBlack,
                        )

                        if (isShareContactOpen) {
                            ShareContact(
                                onDismiss = {
                                    onFocus(false)
                                    isShareContactOpen = false
                                },
                                onSave = {
                                    isShareContactOpen = false
                                    context.openWhatsapp(callLog.number, it)
                                    //callLogNote(number, it)
                                },
                                onFocus = { onFocus(it) },
                                yourBringIntoViewRequester,
                                onValueChange = {

                                    //onNoteValueChange(it)
                                },
                                "",
                                viewModel
                            )
                        }
                    }


                    IconButton(
                        onClick = { context.sendTextMessage(callLog.number) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.text_msg_outlined),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(3.dp),
                            tint = dimBlack
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        IconButton(onClick = {
                            viewModel.onAddNewAlarm(callLog1.callLogDetails.maxByOrNull { it.date }) {
                                isAlarm = true
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.alarm),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(4.dp),
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
                                        android.os.Handler().postDelayed({
                                            isAlarm = false
                                        }, 500)
                                    }
                                })
                        }

                    }
                }/*   Spacer(modifier = Modifier.height(5.dp))
                   Divider(color = Color(0x99E0E2E4))*/
                Spacer(modifier = Modifier.height(5.dp))

            }

            if (isNotePopupOpen1) {
                isTaskPopupOpen = false
                isTaskPopupOpen2 = false
                NotePopup(callLog = callLog, onDismiss = {
                    isNotePopupOpen1 = false
                }, onSave = {
                    isNotePopupOpen1 = false
                    onAddNote(callLog, it)
                }, onFocus = {
                    onFocus(it)
                }, yourBringIntoViewRequester)
            }


            if (isTaskPopupOpen) {
                onFocus(true)
                TasksPopup(viewModel, callLog, onSave = {
                    isTaskPopupOpen = false
                    onTasksChange(it)
                }, onCancel = {
                    isTaskPopupOpen = false
                })
            }


        }
        if (showSaveInappSheet) {
            isTaskPopupOpen = false
            isTaskPopupOpen2 = false
            val tempName = if (callLog.cachedName == "Unknown") "" else callLog.cachedName
            ModalBottomSheet(
                onDismissRequest = { showSaveInappSheet = false },
                sheetState = saveContactSheetState
            ) {
                SaveContactUi(
                    callLog.number,
                    tempName,
                    onClose = {
                        showSaveInappSheet = false
                    },
                    onSave = { name, number ->
                        showSaveInappSheet = false
                        callLog.cachedName = name
                        onContactSavedClick(name, number)
                    },
                    onFocusChangesForName = { onFocus(it) },
                    contactHelper = viewModel.contactHelper
                )
            }
        }
    }

}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMsg: String = "Search notes, name, number...",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp)
    ) {
        IconButton(
            onClick = onSearch, modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = Color(0xFF454C55)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp, bottom = 0.dp),
        ) {
            if (query.isEmpty()) {
                Text(
                    text = emptyMsg,
                    style = normalGoogleSansStyle,
                    fontSize = 14.sp.nonScaledSp,
                    color = Color(0xFF454C55),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterStart)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = { onQueryChange(it) },
                textStyle = normalGoogleSansStyle.copy(fontSize = 14.sp.nonScaledSp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 0.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch()
                }),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp), // Adjusted vertical padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        innerTextField()
                        // Clear text icon at the end
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                })
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NotePopup(
    callLog: CallLogDetails,
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    arrowAlignment: ArrowAlignment = ArrowAlignment.TopLeft,
) {
    val bubbleState = rememberBubbleState(
        alignment = arrowAlignment,
        arrowOffsetY = 15.dp,
        arrowOffsetX = 50.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    LocalFocusManager.current
    var noteText by remember {
        mutableStateOf("")
    }
    var shouldFocus by remember {
        mutableStateOf(true)
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText, selection = TextRange(noteText.length)
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
            text = noteText, selection = TextRange(noteText.length)
        )
        delay(1200)
        focusRequesterForNote.requestFocus()/* textFieldValueState =   TextFieldValue(
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
            .padding(top = 0.dp, start = 10.dp, end = 10.dp)
            .background(Color.Transparent)
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
                        }/*   Handler().postDelayed({
                                                        if (!it.isFocused) {
                                                            shouldFocus = false
                                                            // focusRequesterForNote.freeFocus()
                                                            onDismiss()
                                                        }
                                                 }, 500)
                    */
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
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

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NotePopup2(
    callLog: CallLogDetails,
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    arrowAlignment: ArrowAlignment = ArrowAlignment.TopLeft,
) {
    val bubbleState = rememberBubbleState(
        alignment = arrowAlignment,
        arrowOffsetY = 5.dp,
        arrowOffsetX = 25.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    LocalFocusManager.current
    var noteText by remember {
        mutableStateOf("")
    }
    var shouldFocus by remember {
        mutableStateOf(true)
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText, selection = TextRange(noteText.length)
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
        noteText = if (!callLog.callNote.isNullOrEmpty()) callLog.callNote!! else ""
        textFieldValueState = TextFieldValue(
            text = noteText, selection = TextRange(noteText.length)
        )
        delay(100)
        focusRequesterForNote.requestFocus()/* textFieldValueState =   TextFieldValue(
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
            .padding(top = 0.dp, start = 25.dp, end = 10.dp)
            .background(Color.Transparent)
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
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


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class
)
@Composable
private fun ShareContact(
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    onValueChange: (value: String) -> Unit,
    preNote: String = "",
    viewModel: HomeVm,
) {
    val context = LocalContext.current
    val contactHelper = viewModel.contactHelper
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 10.dp,
        arrowOffsetX = 70.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val noteText by remember {
        mutableStateOf(preNote)
    }
    var contacts by remember {
        mutableStateOf(SnapshotStateMap<String?, String?>())
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText, selection = TextRange(noteText.length)
            )
        )
    }
    LaunchedEffect(true) {
        delay(1200)
        focusRequesterForNote.requestFocus()

    }
    Popup(
        onDismissRequest = {
            focusManager.clearFocus()
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
    ) {
        Column(
            modifier = Modifier
                .padding(top = 5.dp, start = 0.dp, end = 10.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier,
                        border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(3.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Text(
                            text = "Share Contact",
                            color = Orange,
                            fontFamily = montserrat_semibold,
                            modifier = Modifier.padding(6.dp),
                            fontSize = 10.sp.nonScaledSp
                        )
                    }

                    IconButton(onClick = {
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }

                }
                TextField(
                    value = textFieldValueState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                        .focusRequester(focusRequesterForNote)
                        .onFocusChanged {
                            onFocus(it.isFocused)
                        }
                        .onFocusEvent {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    bringIntoViewRequester?.bringIntoView()
                                }
                            }
                        },
                    onValueChange = {
                        textFieldValueState = it
                        //onValueChange(textFieldValueState.text)
                        CoroutineScope(Dispatchers.IO).launch {
                            if (it.text.length >= 3) {
                                if (hasAllRequiredPermissions(context)) {
                                    contacts = contactHelper.searchContacts(it.text)
                                } else {
                                    EventEmitter.postEvent(Event.HomeVm(type = 5))
                                }
                            }
                        }
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
                            text = "Search contact using name,number",
                            color = Color.LightGray,
                            modifier = Modifier,
                            fontFamily = montserrat,
                            fontSize = 12.sp.nonScaledSp
                        )
                    })
                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(modifier = Modifier) {
                    items(contacts.keys.toList()) { key ->
                        Contact(name = contacts[key] ?: "", mobile = key ?: "") {
                            onSave("Number: $key\nName: ${contacts[key]}")
                        }
                    }
                }/*
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            onDismiss()
                                        },
                                        modifier = Modifier.height(30.dp),
                                        border = BorderStroke(0.5.dp, color = Color.Red),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text(text = "Cancel", color = Color.Red, fontSize = 10.sp.nonScaledSp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            onSave(textFieldValueState.text)
                                        },
                                        modifier = Modifier.height(30.dp),
                                        border = BorderStroke(0.5.dp, color = ThemePurple),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text(text = "Save", color = ThemePurple, fontSize = 10.sp.nonScaledSp)
                                    }
                                }
                */
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksItem(
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
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCheckedBox, onCheckedChange = {
                isCheckedBox = it
                onChecked(it)
            })

            HintedTextField(
                value = taskStr, onValueChange = {
                    taskStr = it
                    onTaskValueChange(it)
                }, hint = "Enter a tasks here...", onDone = {
                    onDone()
                }, focusRequester = focusRequester, onDelete = {
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
    viewModel: HomeVm,
    callLog: CallLogDetails,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit,
) {

    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopCenter,
        arrowOffsetY = 10.dp,
        arrowOffsetX = (-60).dp,
        arrowShape = ArrowShape.FullTriangle,
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
                    callLogId = callLog.id, taskId = generateUniqueContactUUID(
                        Math.random().toString()
                    ), "", Date().time, false
                )
            )
            //  viewModel.updateTasks(tempData)
            data = tempData
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 0.dp, start = 0.dp, end = 10.dp)
            .background(Color.Transparent)
            .clickable(enabled = false) { }) {
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
            Card(
                modifier = Modifier,
                border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(3.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Text(
                    text = "ADD TASKS",
                    color = Orange,
                    fontFamily = montserrat_semibold,
                    modifier = Modifier.padding(6.dp),
                    fontSize = 10.sp.nonScaledSp
                )
            }
            Log.e("juhygtf", "TasksPopup: ")
            LazyColumn(modifier = Modifier.height((50.dp * (data.filter { !it.isDelete }.size)))) {
                itemsIndexed(data) { index, s ->
                    val focusRequester = remember { FocusRequester() }
                    focusRequesters[s] = focusRequester
                    if (!s.isDelete) {
                        TasksItem(isChecked = s.strikeThrough, tasks = s, onChecked = {
                            //s.strikeThrough = it
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
                                    callLogId = callLog.id, taskId = generateUniqueContactUUID(
                                        Math.random().toString()
                                    ), "", Date().time, false
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
                                callLogId = callLog.id, taskId = generateUniqueContactUUID(
                                    Math.random().toString()
                                ), "", Date().time, false
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        val tempList = arrayListOf<Tasks>()
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
                        data = tempList
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
    viewModel: HomeVm,
    callLog: CallLogDetails,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit,
) {
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 0.dp,
        arrowOffsetX = 10.dp,
        arrowShape = ArrowShape.FullTriangle,
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
                    callLogId = callLog.id, taskId = generateUniqueContactUUID(
                        Math.random().toString()
                    ), "", Date().time, false
                )
            )
            //  viewModel.updateTasks(tempData)
            data = tempData
        }
    }
    Column(
        modifier = Modifier
            .padding(top = 0.dp, start = 0.dp, end = 10.dp)
            .background(Color.Transparent)
            .clickable(enabled = false) { }) {
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
            Card(
                modifier = Modifier,
                border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(3.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Text(
                    text = "ADD TASKS",
                    color = Orange,
                    fontFamily = montserrat_semibold,
                    modifier = Modifier.padding(6.dp),
                    fontSize = 10.sp.nonScaledSp
                )
            }

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
                                    callLogId = callLog.id, taskId = generateUniqueContactUUID(
                                        Math.random().toString()
                                    ), "", Date().time, false
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
                                callLogId = callLog.id, taskId = generateUniqueContactUUID(
                                    Math.random().toString()
                                ), "", Date().time, false
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        /*     val tempList = arrayListOf<Tasks>()
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

