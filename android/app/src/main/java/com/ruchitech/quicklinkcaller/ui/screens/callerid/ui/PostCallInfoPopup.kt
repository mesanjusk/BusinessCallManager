package com.ruchitech.quicklinkcaller.ui.screens.callerid.ui

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.copyToClipboard
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.ReminderActivity
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.HintedTextField
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TimePickerPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.roundToInt


@OptIn(
    DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun PostCallInfoPopup(
    dbRepository: DbRepository,
    number: String,
    openWhatsapp: (number: String) -> Unit,
    openTextMsg: (number: String) -> Unit,
    callBack: (number: String) -> Unit,
    saveNumberInApp: (number: String) -> Unit,
    saveNumberInPhonebook: (number: String) -> Unit,
    callLogNote: (number: String, note: String) -> Unit,
    onClose: () -> Unit,
    toastMsg: (msg: String) -> Unit,
    onReminder: (mergeDate: String, date: String) -> Unit,
    onHideUi: (show: Boolean) -> Unit,
    onFocusChangesForNote: (isFocused: Boolean) -> Unit,
    onMinimize: () -> Unit,
    onNumberSwitch: () -> Unit,
    notesList: MutableMap<String, String>,
    onNoteValueChange: (value: String) -> Unit,
    onShareContact: (name: String?, number: String) -> Unit,
    callType: com.ruchitech.quicklinkcaller.helper.CallType,
    callLogHelper: CallLogHelper,
    tasksList: MutableMap<String, List<Tasks?>>,
    onTasksValueChange: (tasks: List<Tasks?>) -> Unit,
    onTasksSave: (tasks: List<Tasks?>) -> Unit,
    defaultType:Int = 0,
    onNoteTabChange: (type:Int) -> Unit,  //0 for notes 1 for tasks
    contactHelper: ContactHelper
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var noteStr by remember {
        mutableStateOf(MyApp.instance.tempNote)
    }
    var noteStrError by remember {
        mutableStateOf(false)
    }
    var reminderVisible by remember {
        mutableStateOf(false)
    }
    var numberFrom by remember {
        mutableIntStateOf(0)  //1 for phonebook, 2 in-app book, 3 for both side saved // 0 unknown
    }
    var colorForName by remember { mutableStateOf(Orange) }

    var contactName by remember { mutableStateOf("") }
    val reminder by remember {
        mutableStateOf<Reminders?>(null)
    }

    var postCallType by remember {
        mutableStateOf(CallType.LOADING)
    }
    var callTime by remember {
        mutableStateOf("")
    }

    var tasks by remember {
        mutableStateOf(tasksList)
    }

    DisposableEffect(number) {
        noteStr = notesList[number] ?: ""
        val job = GlobalScope.launch(Dispatchers.IO) {
            var checkName: String? = ""
            val contactDetails =
                contactHelper.getNameFromPhoneNumber(number) /// ContactHelper(context).getContactDetailsByPhoneNumber(number)
            if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                checkName = dbRepository.contact.getContactByPhoneNumber(number)?.contact_title
                contactName = if (!checkName.isNullOrEmpty()) {
                    numberFrom = 2
                    colorForName = Orange
                    checkName
                } else {
                    numberFrom = 0
                    "Unknown"
                }
            } else {
                numberFrom = 1
                val alsoCheckInAppDB =
                    dbRepository.contact.getContactByPhoneNumber(number)?.contact_title
                if (!alsoCheckInAppDB.isNullOrEmpty()) {
                    numberFrom = 3
                    //    checkName = contactDetails.displayName
                }
                contactName = contactDetails.ifEmpty { "Unknown" }
                // contactName = checkName ?: "Unknown - code102"
            }
            /*   reminder = dbRepository.reminder.getReminderByCallerId(number)
               val callLogData = dbRepository.callLogDao.getCallLogsByCallerId(number)
               if (callLogData != null) {
                   noteStr = callLogData.callNote ?: ""
               }*/


            when (numberFrom) {
                0, 2 -> {
                    colorForName = Orange
                }

                1, 3 -> {
                    colorForName = dimBlack
                }
            }

            if (callType is com.ruchitech.quicklinkcaller.helper.CallType.PostCall) {
                callLogHelper.insertRecentCallLogs {}
                delay(100)
                val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(number)
                if (callerIdExists > 0) {
                    val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(number)
                    if (callLogs != null) {
                        postCallType = callLogs.type
                        callTime = formatTimeAgo(callLogs.date)
                        tasks[number] = callLogs.tasks
                    }
                }
            }
        }
        onDispose {
            job.cancel()
        }
    }

    val state = rememberDateRangePickerState()
    var showRangepicker by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isContactsPopupOpen = remember {
        mutableStateOf(false)
    }
    var isNotePopupOpen by remember {
        mutableStateOf(false)
    }
    var isTaskPopupOpen by remember {
        mutableStateOf(false)
    }
    var isShareContactOpen by remember {
        mutableStateOf(false)
    }

    if (reminderVisible) {
        onHideUi(true)
        MyApp.instance.tempNote = noteStr
        val intent = Intent(context, ReminderActivity::class.java)
        intent.putExtra("reminder", Gson().toJson(reminder))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }

    val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    var isAlarm by remember {
        mutableStateOf(false)
    }
    var isDismissedByIcon by remember {
        mutableStateOf(false)
    }
    var expanded by remember {
        mutableStateOf(false)
    }
    var enabledTab by remember {
        mutableIntStateOf(defaultType)
    }
    Box(modifier = Modifier.padding(bottom = 20.dp)) {
        Box(modifier = Modifier, contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 25.dp, start = 25.dp, end = 25.dp)
                    .background(color = Color.Transparent, shape = RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 0.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box {
                            Column {
                                Spacer(modifier = Modifier.height(0.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp)
                                        .padding(
                                            start = 0.dp,
                                            end = 10.dp,
                                            bottom = 0.dp,
                                            top = 0.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.7F)) {
                                        Row(
                                            modifier = Modifier.padding(start = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = when (callType) {
                                                    com.ruchitech.quicklinkcaller.helper.CallType.IncomingCall -> painterResource(
                                                        id = R.drawable.ic_incoming
                                                    )

                                                    com.ruchitech.quicklinkcaller.helper.CallType.OutgoingCall -> painterResource(
                                                        id = R.drawable.ic_outgoing
                                                    )

                                                    com.ruchitech.quicklinkcaller.helper.CallType.PostCall -> {
                                                        when (postCallType) {
                                                            CallType.INCOMING -> painterResource(
                                                                id = R.drawable.ic_incoming
                                                            )

                                                            CallType.OUTGOING -> painterResource(
                                                                id = R.drawable.ic_outgoing
                                                            )

                                                            CallType.MISSED -> painterResource(
                                                                id = R.drawable.ic_misscall
                                                            )

                                                            CallType.LOADING -> painterResource(
                                                                id = R.drawable.ic_loading
                                                            )
                                                        }
                                                    }
                                                },
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .padding()
                                                    .scale(1F),
                                                tint = if (postCallType == CallType.MISSED) Color.Red else Color.LightGray,
                                            )

                                            Text(
                                                text = contactName,
                                                fontFamily = montserrat_semibold,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp.nonScaledSp,
                                                overflow = TextOverflow.Ellipsis,
                                                color = colorForName,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .padding(end = 8.dp, start = 5.dp)
                                                    .weight(1f)
                                            )

                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        ) {

                                            Text(
                                                text = number,
                                                fontWeight = FontWeight.Bold,
                                                overflow = TextOverflow.Ellipsis,
                                                fontFamily = montserrat,
                                                color = Color.Gray,
                                                fontSize = 10.sp.nonScaledSp,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .padding(start = 10.dp, end = 8.dp, top = 0.dp)
                                            )

                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_copy),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .padding(2.dp)
                                                    .scale(1F)
                                                    .clickable {
                                                        context.copyToClipboard(number)
                                                    },
                                                tint = Color.LightGray,
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_share),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .padding(2.dp)
                                                    .scale(1F)
                                                    .clickable {
                                                        onShareContact(contactName, number)
                                                    },
                                                tint = Color.LightGray,
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            /*
                                                                                        Box {
                                                                                            Icon(
                                                                                                painter = painterResource(id = R.drawable.ic_share_contacts),
                                                                                                contentDescription = null,
                                                                                                modifier = Modifier
                                                                                                    .size(25.dp)
                                                                                                    .padding(1.dp)
                                                                                                    .scale(1F)
                                                                                                    .clickable {
                                                                                                        isShareContactOpen = true
                                                                                                    },
                                                                                                tint = Color.LightGray,
                                                                                            )

                                                                                            if (isShareContactOpen) {
                                                                                                ShareContact(
                                                                                                    onDismiss = {
                                                                                                        onFocusChangesForNote(false)
                                                                                                        isShareContactOpen = false
                                                                                                    },
                                                                                                    onSave = {
                                                                                                        isShareContactOpen = false
                                                                                                        context.openWhatsapp(number, it)
                                                                                                        //callLogNote(number, it)
                                                                                                    },
                                                                                                    onFocus = { onFocusChangesForNote(it) },
                                                                                                    yourBringIntoViewRequester,
                                                                                                    onValueChange = {

                                                                                                        //onNoteValueChange(it)
                                                                                                    }, ""
                                                                                                )
                                                                                            }
                                                                                        }
                                            */
                                        }
                                    }
                                    Text(
                                        text = callTime,
                                        fontWeight = FontWeight.Bold,
                                        overflow = TextOverflow.Ellipsis,
                                        fontFamily = montserrat,
                                        fontSize = 10.sp.nonScaledSp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(end = 8.dp, top = 5.dp)

                                    )
                                }
                                Divider(color = Color(0x99E0E2E4))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(35.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        IconButton(onClick = {
                                            isContactsPopupOpen.value = true
                                        }) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_phonebook),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .padding(3.dp)
                                            )
                                        }
                                        if (isContactsPopupOpen.value) {
                                            val bubbleState = rememberBubbleState(
                                                alignment = ArrowAlignment.BottomLeft,
                                                arrowOffsetY = 1.dp,
                                                arrowOffsetX = 25.dp,
                                                cornerRadius = 8.dp
                                            )
                                            Popup(
                                                onDismissRequest = {
                                                    isContactsPopupOpen.value = false
                                                },
                                                properties = PopupProperties(
                                                    dismissOnClickOutside = true,
                                                    clippingEnabled = false,
                                                    usePlatformDefaultWidth = true,
                                                    focusable = false
                                                ),
                                                alignment = Alignment.TopStart,
                                                offset = IntOffset(0, (-150).dp.value.roundToInt())
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(bottom = 20.dp)
                                                        .background(Color.Transparent)
                                                        .clickable(
                                                            indication = null,
                                                            interactionSource = MutableInteractionSource()
                                                        ) {
                                                            isContactsPopupOpen.value = false
                                                        }
                                                ) {

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
                                                                    Color.White,
                                                                    shape = RoundedCornerShape(5.dp)
                                                                )
                                                                .padding(
                                                                    horizontal = 5.dp,
                                                                    vertical = 8.dp
                                                                )
                                                                .clickable {
                                                                    isContactsPopupOpen.value =
                                                                        false
                                                                    if (numberFrom == 1 || numberFrom == 3) toastMsg(
                                                                        "Number already saved"
                                                                    ) else saveNumberInPhonebook(
                                                                        number
                                                                    )
                                                                },
                                                            color = if (numberFrom == 1 || numberFrom == 3) Color.Gray else ThemePurple,
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
                                                                    Color.White,
                                                                    shape = RoundedCornerShape(5.dp)
                                                                )
                                                                .padding(
                                                                    horizontal = 5.dp,
                                                                    vertical = 8.dp
                                                                )
                                                                .clickable {
                                                                    isContactsPopupOpen.value =
                                                                        false
                                                                    if (numberFrom == 2 || numberFrom == 3) toastMsg(
                                                                        "Number already saved"
                                                                    ) else saveNumberInApp(
                                                                        number
                                                                    )
                                                                },
                                                            color = if (numberFrom == 2 || numberFrom == 3) Color.Gray else ThemePurple,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(30.dp))
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(0.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        IconButton(onClick = {
                                            isContactsPopupOpen.value = false
                                            isNotePopupOpen = true
                                        }) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_notes),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .padding(3.dp)
                                            )
                                        }
                                        if (isNotePopupOpen) {

                                            val bubbleState = rememberBubbleState(
                                                alignment = ArrowAlignment.TopLeft,
                                                arrowOffsetY = 25.dp,
                                                arrowOffsetX = 18.dp,
                                                arrowShape = ArrowShape.FullTriangle,
                                                cornerRadius = 8.dp
                                            )

                                            Popup(
                                                onDismissRequest = {

                                                },
                                                alignment = Alignment.TopStart,
                                                offset = IntOffset(0, (20).dp.value.roundToInt()),
                                                properties = PopupProperties(
                                                    clippingEnabled = false,
                                                    focusable = true,
                                                    dismissOnBackPress = true,
                                                    dismissOnClickOutside = false,
                                                    usePlatformDefaultWidth = true
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(
                                                            top = 0.dp,
                                                            start = 0.dp,
                                                            end = 10.dp
                                                        )
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
                                                        Row {
                                                            Card(
                                                                onClick = { enabledTab = 0 },
                                                                modifier = Modifier,
                                                                border = BorderStroke(
                                                                    0.5.dp,
                                                                    color = Color(0xFFD8D9DB)
                                                                ),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (enabledTab == 0) Color.White else Color.LightGray
                                                                ),
                                                                shape = RoundedCornerShape(3.dp),
                                                                elevation = CardDefaults.cardElevation(
                                                                    1.dp
                                                                )
                                                            ) {
                                                                Text(
                                                                    text = "ADD NOTES",
                                                                    color = if (enabledTab == 0) Orange else Color.DarkGray,
                                                                    fontFamily = montserrat_semibold,
                                                                    modifier = Modifier.padding(6.dp),
                                                                    fontSize = 10.sp.nonScaledSp
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(20.dp))
                                                            Card(
                                                                onClick = {
                                                                    enabledTab = 1
                                                                },
                                                                modifier = Modifier,
                                                                border = BorderStroke(
                                                                    0.5.dp,
                                                                    color = Color(0xFFD8D9DB)
                                                                ),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (enabledTab == 1) Color.White else Color.LightGray
                                                                ),
                                                                shape = RoundedCornerShape(3.dp),
                                                                elevation = CardDefaults.cardElevation(
                                                                    1.dp
                                                                )
                                                            ) {
                                                                Text(
                                                                    text = "ADD TASKS",
                                                                    color = if (enabledTab == 1) Orange else Color.DarkGray,
                                                                    fontFamily = montserrat_semibold,
                                                                    modifier = Modifier.padding(6.dp),
                                                                    fontSize = 10.sp.nonScaledSp
                                                                )
                                                            }

                                                        }
                                                        ///
                                                        if (enabledTab == 0) {
                                                            onNoteTabChange(enabledTab)
                                                            NotePopup(
                                                                onDismiss = {
                                                                    onFocusChangesForNote(false)
                                                                    isNotePopupOpen = false
                                                                },
                                                                onSave = {
                                                                    isNotePopupOpen = false
                                                                    callLogNote(number, it)
                                                                },
                                                                onFocus = { onFocusChangesForNote(it) },
                                                                yourBringIntoViewRequester,
                                                                onValueChange = {
                                                                    onNoteValueChange(it)
                                                                }, noteStr
                                                            )
                                                        } else {
                                                            onNoteTabChange(enabledTab)
                                                            TasksPopup(
                                                                tasks = tasks[number]
                                                                    ?: emptyList(),
                                                                onSave = {
                                                                    isNotePopupOpen = false
                                                                    onTasksSave(it)
                                                                }, onValueChange = {
                                                                    onTasksValueChange(it)
                                                                }, onCancel = {
                                                                    isNotePopupOpen = false
                                                                })
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                    /*    Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight((2f))
                                        ) {
    *//*
                                        IconButton(onClick = {
                                            isContactsPopupOpen.value = false
                                            isNotePopupOpen = false
                                            isTaskPopupOpen = true
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_tasks),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .padding(1.dp),
                                                tint = ThemePurple

                                            )
                                        }
                                        if (isTaskPopupOpen) {
                                            TasksPopup(
                                                tasks = tasks[number] ?: emptyList(),
                                                onSave = {
                                                    isTaskPopupOpen = false
                                                    onTasksSave(it)
                                                }, onValueChange = {
                                                    onTasksValueChange(it)
                                                }, onCancel = {
                                                    isTaskPopupOpen = false
                                                })
                                        }
*//*
                                    }*/
                                    /* Row(
                                         modifier = Modifier.fillMaxWidth().weight(1f),
                                         horizontalArrangement = Arrangement.End,
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {*/
                                    IconButton(onClick = {
                                        callBack(number)
                                    }, modifier = Modifier.weight(1f)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_call_quick),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(top = 2.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        openWhatsapp(number)
                                    }, modifier = Modifier.weight(1f)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_whatsapp),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(3.dp)
                                        )
                                    }

                                    /*                         IconButton(
                                                                 onClick = { openTextMsg(number) },
                                                                 modifier = Modifier.weight(1f)
                                                             ) {
                                                                 Image(
                                                                     painter = painterResource(id = R.drawable.ic_msg),
                                                                     contentDescription = null,
                                                                     modifier = Modifier
                                                                         .size(25.dp)
                                                                         .padding(3.dp)
                                                                 )
                                                             }*/

                                    Box(modifier = Modifier.weight(1f)) {
                                        IconButton(onClick = {
                                            expanded = !expanded
                                        }) {
                                            Image(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .padding(3.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = {
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    expanded = false
                                                }, 100)
                                            },
                                            properties = PopupProperties(
                                                clippingEnabled = true,
                                                dismissOnClickOutside = true
                                            ),
                                            offset = DpOffset(0.dp, 10.dp),
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .background(Color.White)
                                                .padding(end = 10.dp)
                                        ) {
                                            DropdownMenuItem(onClick = {
                                                expanded = false
                                                openTextMsg(number)
                                                //  viewModel.exportCallLogsIntoPdf()
                                            },
                                                leadingIcon = {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_msg),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(5.dp)
                                                    )
                                                }, text = {
                                                    Text(
                                                        "Text Message",
                                                        fontFamily = montserrat_semibold,
                                                        fontSize = 10.sp.nonScaledSp
                                                    )
                                                }, modifier = Modifier.height(25.dp)
                                            )
                                            Divider(
                                                modifier = Modifier.padding(start = 10.dp),
                                                thickness = 0.5.dp
                                            )
                                            DropdownMenuItem(onClick = {
                                                expanded = false
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val log =
                                                        dbRepository.callLogDao.getLastEntryByCallerId(
                                                            number
                                                        )

                                                    if (log != null) {
                                                        isAlarm = true
                                                    }
                                                    Log.e("dkjgshjfgf", "onStartCommand: $log")
                                                }
                                                //  viewModel.exportCallLogsIntoPdf()
                                            },
                                                leadingIcon = {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_alarm),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(5.dp)
                                                    )
                                                }, text = {
                                                    Text(
                                                        "Reminder",
                                                        fontFamily = montserrat_semibold,
                                                        fontSize = 10.sp.nonScaledSp
                                                    )
                                                }, modifier = Modifier.height(25.dp)
                                            )

                                            Divider(
                                                modifier = Modifier.padding(start = 10.dp),
                                                thickness = 0.5.dp
                                            )
                                            DropdownMenuItem(onClick = {
                                                expanded = false
                                                isShareContactOpen = true
                                                //  viewModel.exportCallLogsIntoPdf()
                                            },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_share_contacts),
                                                        contentDescription = null,
                                                        tint = ThemePurple,
                                                        modifier = Modifier.padding(3.dp)
                                                    )
                                                }, text = {
                                                    Text(
                                                        "Contact Sharing",
                                                        fontFamily = montserrat_semibold,
                                                        fontSize = 10.sp.nonScaledSp
                                                    )
                                                }, modifier = Modifier.height(25.dp)
                                            )
                                        }
                                        if (isAlarm) {
                                            TimePickerPopup(
                                                onDismiss = { isAlarm = false },
                                                selectedTime = { hour, minute, seconds, mode, date ->
                                                    val mergeStr =
                                                        "$hour:$minute:$seconds $mode $date"
                                                    if (date != null) {
                                                        onReminder(mergeStr, date)
                                                    }
                                                    Handler().postDelayed({
                                                        isAlarm = false
                                                    }, 300)
                                                })
                                        }

                                        if (isShareContactOpen) {
                                            ShareContact(
                                                onDismiss = {
                                                    onFocusChangesForNote(false)
                                                    isShareContactOpen = false
                                                },
                                                onSave = {
                                                    isShareContactOpen = false
                                                    context.openWhatsapp(number, it)
                                                    //callLogNote(number, it)
                                                },
                                                onFocus = { onFocusChangesForNote(it) },
                                                yourBringIntoViewRequester,
                                                onValueChange = {

                                                    //onNoteValueChange(it)
                                                }, "",contactHelper
                                            )
                                        }

                                    }
                                    //   }
                                }

                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f) // Adjust the scale as needed
                        .padding(0.dp)
                        .size(22.dp)
                        .clip(shape = RoundedCornerShape(25.dp))
                        .background(color = Color.White)
                        .clickable { onClose() }
                )
                Spacer(modifier = Modifier.width(25.dp))
                Icon(
                    painter = painterResource(id = R.drawable.minus),
                    contentDescription = "Close",
                    modifier = Modifier
                        .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f) // Adjust the scale as needed
                        .size(22.dp)
                        .clip(shape = RoundedCornerShape(25.dp))
                        .background(color = Color.White)
                        .clickable { onMinimize() }
                )
            }
        }
    }
}


@OptIn(
    ExperimentalFoundationApi::class
)
@Composable
private fun NotePopup(
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    onValueChange: (value: String) -> Unit,
    preNote: String = ""
) {
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 25.dp,
        arrowOffsetX = 18.dp,
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
        mutableStateOf(preNote)
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText,
                selection = TextRange(noteText.length)
            )
        )
    }
    LaunchedEffect(true) {
        delay(1200)
        focusRequesterForNote.requestFocus()
    }
    /*    Popup(
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
        ) {*/
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF2E0))
            .padding(0.dp)
    ) {

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
                onValueChange(textFieldValueState.text)
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
        Spacer(modifier = Modifier.height(10.dp))
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
    }

    //}
    //   }


}


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
private fun ShareContact(
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    onValueChange: (value: String) -> Unit,
    preNote: String = "",
    contactHelper: ContactHelper
) {
    val context = LocalContext.current
   // val contactHelper = ContactHelper(context)
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopRight,
        arrowOffsetY = 25.dp,
        arrowOffsetX = ((-18).dp),
        arrowShape = ArrowShape.HalfTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var noteText by remember {
        mutableStateOf(preNote)
    }
    var contacts by remember {
        mutableStateOf(SnapshotStateMap<String?, String?>())
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText,
                selection = TextRange(noteText.length)
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
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, (5).dp.value.roundToInt()),
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
                .padding(top = 10.dp, start = 0.dp, end = 10.dp)
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
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
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
                                contacts = contactHelper.searchContacts(it.text)
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
                }
                /*
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

@Composable
fun Contact(name: String, mobile: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        onClick()
    }) {
        Column {
            Text(
                text = name,
                fontFamily = montserrat_semibold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp.nonScaledSp,
                overflow = TextOverflow.Ellipsis,
                color = Orange,
                lineHeight = 1.sp,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = 5.dp, end = 8.dp, top = 5.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
                    .height(20.dp)
                    .background(
                        Color.Transparent
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mobile,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = montserrat,
                    lineHeight = 1.sp,
                    color = Color.Gray,
                    fontSize = 11.sp.nonScaledSp,
                    maxLines = 1
                )

                /*           IconButton(onClick = { onClick() }) {
                               Icon(
                                 imageVector = Icons.Default.Send,
                                   contentDescription = null,
                                   modifier = Modifier
                                       .size(18.dp)
                                       .padding(0.dp),
                                   tint = Color.LightGray
                               )
                           }*/
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
        }
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
    onTaskValueChange: (value: String) -> Unit
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TasksPopup(
    tasks: List<Tasks?>,
    onSave: (newList: MutableList<Tasks>) -> Unit,
    onCancel: () -> Unit,
    onValueChange: (newList: List<Tasks?>) -> Unit
) {
    var data by remember {
        mutableStateOf(SnapshotStateList<Tasks?>())
    }
    LaunchedEffect(true) {
        data.addAll(tasks)
    }
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 15.dp,
        arrowOffsetX = 20.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    if (data.isNullOrEmpty()) {
        LaunchedEffect(true) {
            data.add(
                Tasks(
                    callLogId = 91314141,
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
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF2E0))
            .padding(0.dp)
    ) {
        val focusRequesters = remember { mutableStateMapOf<Tasks, FocusRequester>() }
        Log.e("juhygtf", "TasksPopup: ")
        LazyColumn(modifier = Modifier) {
            itemsIndexed(data) { index, s ->
                val focusRequester = remember { FocusRequester() }
                focusRequesters[s!!] = focusRequester
                if (!s.isDelete) {
                    TasksItem(isChecked = s.strikeThrough, tasks = s, onChecked = {
                        s.strikeThrough = it
                        onValueChange(data)
                    }, onDelete = {
                        s.isDelete = true
                        "${s.tasks}9131414139"
                        data[index] =
                            s.copy(isDelete = true, tasks = "${s.tasks}9131414139")

                        val lastUndeletedTaskIndex = data.indexOfLast { !it!!.isDelete }
                        if (lastUndeletedTaskIndex != -1) {
                            val item = data[lastUndeletedTaskIndex]
                            focusRequesters[item]?.requestFocus()
                        }
                    }, onDone = {
                        focusRequesters.clear()
                        data.add(
                            Tasks(
                                callLogId = 9131414139,
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
                    }, focusRequester = focusRequester, onTaskValueChange = {
                        s.tasks = it
                        onValueChange(data)
                    })

                }
                if (index == data.size.minus(1)) {
                    LaunchedEffect(Unit) {
                        delay(100)
                        val lastUndeletedTaskIndex = data.indexOfLast { !it!!.isDelete }
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
                    data.add(
                        Tasks(
                            callLogId = 91314141,
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
                    val tempList = SnapshotStateList<Tasks?>()
                    data.forEach {
                        if (it!!.isDelete) {
                            it.isDelete = false
                            it.tasks = it.tasks?.split("9131414139")?.get(0)
                        }

                        if (!it.tasks.isNullOrEmpty()) {
                            tempList.add(it)
                        }

                    }
                    data = tempList
                    onCancel()
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
                    val tempList = arrayListOf<Tasks>()
                    data.forEach {
                        if (!it!!.tasks.isNullOrEmpty() && !it.isDelete) {
                            tempList.add(it)
                        }
                    }
                    onSave(tempList)
                },
                modifier = Modifier.height(30.dp),
                border = BorderStroke(0.5.dp, color = ThemePurple),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(text = "Save", color = ThemePurple, fontSize = 10.sp.nonScaledSp)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

    }


}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
private fun NoteAndTasks(
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    onValueChange: (value: String) -> Unit,
    preNote: String = ""
) {


}


