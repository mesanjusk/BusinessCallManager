package com.ruchitech.quicklinkcaller

import SaveContactUi
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.CallType
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.copyToClipboard
import com.ruchitech.quicklinkcaller.helper.formatDateFromMillis
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.helper.shareContact
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.recievers.AlarmReceiver
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_ADD_LEAD
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_ADD_NOTE
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_CALL
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_COPY_NUMBER
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_REMINDER
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_SAVE_CONTACT
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_SHARE_CONTACT
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_SHARE_NUMBER
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_TEXT_MSG
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_WHATSAPP
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.room.data.TempDataEntity
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TimePickerPopup2
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.TasksItem
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PostCallActivity : ComponentActivity() {
    @Inject
    lateinit var appPreference: AppPreference

    @Inject
    lateinit var callLogHelper: CallLogHelper
    @Inject
    lateinit var contactHelper: ContactHelper

    @Inject
    lateinit var dbRepository: DbRepository
    private var dateTimeString = ""
    private var callerId = ""
    private val ONE: Int by lazy { 1 }
    private val TWO: Int by lazy { 2 }
    private val ZERO: Int by lazy { 0 }
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()


    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var number = intent.extras?.getString("number")
        val parsedMap = normalizePhoneNumberToMap(
            number!!.replace("", ""),
            "IN"
        )

        callerId = parsedMap["number"] as? String ?: ""
        number = callerId

        val callType = when (intent?.getStringExtra("type")?.toInt()) {
            ONE -> CallType.IncomingCall
            TWO -> CallType.OutgoingCall
            ZERO -> CallType.PostCall
            else -> CallType.IncomingCall
        }


        if (intent.action == ACTION_REMINDER) {
            CoroutineScope(Dispatchers.IO).launch {
                val log =
                    dbRepository.callLogDao.getLastEntryByCallerId(
                        number!!
                    )

                if (log != null) {

                }

            }
        }
        setContent {

            var enabledTab by remember {
                mutableIntStateOf(appPreference.defaultTabForNoteTasks)
            }
            val tasks by remember {
                mutableStateOf(mutableMapOf<String, List<Tasks?>>())
            }

            val scope = rememberCoroutineScope()
            when (intent.action) {

                ACTION_ADD_LEAD -> {
                    AddLeadDialog(
                        phone = number ?: "",
                        onDismiss = { finish() },
                        onSave = { name, status ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val userUuid = appPreference.userId ?: return@launch
                                val lead = Lead(
                                    lead_uuid = java.util.UUID.randomUUID().toString(),
                                    user_uuid = userUuid,
                                    phone = number ?: "",
                                    name = name.ifBlank { null },
                                    source = "call",
                                    status = status
                                )
                                dbRepository.leadDao.insertLead(lead)
                                CoroutineScope(Dispatchers.Main).launch {
                                    android.widget.Toast.makeText(
                                        this@PostCallActivity,
                                        "Lead saved!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        }
                    )
                }

                ACTION_TEXT_MSG -> {
                    number?.let { sendTextMessage(it) }
                    finish()
                }

                ACTION_COPY_NUMBER -> {
                    val number = intent.getStringExtra("number")
                    val contactName = intent.getStringExtra("contactName")
                    copyToClipboard(number!!)
                    finish()
                }

                ACTION_SHARE_NUMBER -> {
                    val number = intent.getStringExtra("number")
                    val contactName = intent.getStringExtra("contactName")
                    shareContact(contactName ?: "", number!!)
                    finish()
                }

                ACTION_CALL -> {
                    val number = intent.getStringExtra("number")
                    if (number != null) {
                        makePhoneCall(number)
                        finish()
                    }
                }

                ACTION_WHATSAPP -> {
                    number?.let { openWhatsapp(it) }
                    finish()
                }

                ACTION_ADD_NOTE -> {
                    // NoteUI()
                    Dialog(
                        onDismissRequest = {
                            finish()
                        }
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
                                    appPreference.defaultTabForNoteTasks = 0
                                    NoteUI(callType)
                                } else {
                                    appPreference.defaultTabForNoteTasks = 1
                                    TasksPopup(
                                        tasks = tasks[number] ?: emptyList(),
                                        onSave = {
                                            scope.launch {
                                                number?.let { it1 ->
                                                    if (callType != CallType.PostCall) {
                                                        val getTempData =
                                                            dbRepository.tempData.getCallerIdOptions()
                                                        getTempData?.tempTasksMap?.set(it1, it)
                                                        if (getTempData != null) {
                                                            dbRepository.tempData.insertOrUpdateCallerIdOptions(
                                                                getTempData
                                                            )
                                                        } else {
                                                            val createTempData = TempDataEntity(
                                                                1,
                                                                mutableMapOf(),
                                                                mutableMapOf()
                                                            )
                                                            createTempData.tempTasksMap[it1] = it
                                                            dbRepository.tempData.insertOrUpdateCallerIdOptions(
                                                                createTempData
                                                            )
                                                        }
                                                        finish()
                                                    } else {
                                                        insertTasksOnCallLog(
                                                            it,
                                                            it1
                                                        )
                                                    }

                                                }
                                            }
                                        }, onValueChange = {

                                        }, onCancel = {
                                            finish()
                                        })
                                }

                            }
                        }
                    }

                }

                ACTION_SHARE_CONTACT -> {
                    ShareContactUi(number = number!!, onDismiss = {
                        finish()
                    }, onSave = {
                        finish()
                    })
                }

                ACTION_SAVE_CONTACT -> {
                    var saveInAppDialog by remember {
                        mutableStateOf(false)
                    }
                    var contactName by remember {
                        mutableStateOf("")
                    }
                    var numberFrom by remember {
                        mutableIntStateOf(0)
                    }
                    DisposableEffect(number) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            var checkName: String? = ""
                            val contactDetails =
                               contactHelper.getNameFromPhoneNumber(number!!) /// ContactHelper(context).getContactDetailsByPhoneNumber(number)
                            if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                                checkName =
                                    dbRepository.contact.getContactByPhoneNumber(number)?.contact_title
                                contactName = if (!checkName.isNullOrEmpty()) {
                                    numberFrom = 2
                                    checkName
                                } else {
                                    numberFrom = 0
                                    ""
                                }
                            } else {
                                numberFrom = 1
                                val alsoCheckInAppDB =
                                    dbRepository.contact.getContactByPhoneNumber(number)?.contact_title
                                if (!alsoCheckInAppDB.isNullOrEmpty()) {
                                    numberFrom = 3
                                    //    checkName = contactDetails.displayName
                                }
                                contactName = contactDetails.ifEmpty { "" }
                                // contactName = checkName ?: "Unknown - code102"
                            }
                            /*   reminder = dbRepository.reminder.getReminderByCallerId(number)
                               val callLogData = dbRepository.callLogDao.getCallLogsByCallerId(number)
                               if (callLogData != null) {
                                   noteStr = callLogData.callNote ?: ""
                               }*/

                            if (callType is CallType.PostCall) {
                                callLogHelper.insertRecentCallLogs {}
                                delay(100)
                                val callerIdExists =
                                    dbRepository.callLogDao.doesCallerIdExist(number)
                                if (callerIdExists > 0) {
                                    val callLogs =
                                        dbRepository.callLogDao.getLastEntryByCallerId(number)
                                }
                            }
                        }
                        onDispose {
                            job.cancel()
                        }
                    }

                    SaveContact(
                        defaultName = contactName,
                        numberFrom = numberFrom,
                        saveNumberInPhonebook = {
                            saveNumberToContacts(number!!, "")
                        }) {
                        saveInAppDialog = true
                    }

                    if (saveInAppDialog) {
                        SaveContactUi(number, onClose = {
                            saveInAppDialog = false
                        }, onSave = { name, number ->
                            saveInAppDialog = false
                            contactName = name
                            saveContactInApp(name, number, "")
                            Toast.makeText(
                                this,
                                "Number saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }, onFocusChangesForName = {
                            if (it) {
                                Handler().postDelayed({
                                    // openKeyboardWithoutFocus()
                                }, 1000)
                            }
                        }, callingByService = true, contactHelper = contactHelper)
                    }


                }


                ACTION_REMINDER -> {
                    TimePickerPopup2(
                        onDismiss = { finish() },
                        selectedTime = { hour, minute, seconds, mode, date ->
                            val mergeStr =
                                "$hour:$minute:$seconds $mode $date"
                            if (date != null) {
                                number?.let {
                                    setAlarm(mergeStr, date, it)
                                }
                            }
                            Handler().postDelayed({
                                finish()
                            }, 300)
                        })
                }

                ACTION_TEXT_MSG -> {
                    number.let { sendTextMessage(it) }
                }

                ACTION_WHATSAPP -> {
                    number.let { openWhatsapp(it) }
                }

                ACTION_CALL -> {
                    number.let { makePhoneCall(it) }
                }

            }
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setGravity(Gravity.CENTER)
        window.attributes.dimAmount = 0.7f
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }

    private fun normalizePhoneNumberToMap(
        phoneNumber: String,
        defaultRegion: String
    ): Map<String, Any?> {
        return try {
            val number = phoneUtil.parse(phoneNumber, defaultRegion)
            if (phoneUtil.isValidNumber(number)) {
                val regionCode = phoneUtil.getRegionCodeForNumber(number)
                val numberType = phoneUtil.getNumberType(number).name

                mapOf(
                    "countryCode" to number.countryCode.toString(),
                    "number" to number.nationalNumber.toString(),
                    "regionCode" to regionCode, // Region code (e.g., IN, US)
                    "type" to numberType, // Type of the number (e.g., MOBILE, FIXED_LINE)
                    "isValid" to true // Indicates if the number is valid
                )
            } else {
                mapOf(
                    "countryCode" to null,
                    "number" to phoneNumber, // Return the input number as-is
                    "regionCode" to null,
                    "type" to null,
                    "isValid" to false // Indicates the number is invalid
                )
            }
        } catch (e: NumberParseException) {
            mapOf(
                "countryCode" to null,
                "number" to phoneNumber, // Return the input number as-is
                "regionCode" to null,
                "type" to null,
                "isValid" to false // Indicates parsing failure
            )
        }
    }


    private fun saveContactInApp(name: String, number: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(
                com.ruchitech.quicklinkcaller.room.data.Contact(
                    contact_title = name,
                    contact_mobile = number,
                    email = email,
                    address = "",
                    user_uuid = appPreference.userId,
                    created_at = formatDateFromMillis(Date().time)
                )
            )
            /*runOnUiThread {
                Toast.makeText(this@PostCallActivity, "Contact Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }*/
        }
    }

    private fun convertTimeStringToMillis(timeString: String): Long {
        val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(timeString)
        return date?.time ?: 0
    }

    private fun convertMillisTo24HourFormat(inMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = inMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)

        return String.format("%02d:%02d", hour, minute)
    }

    private fun setAlarm(selectedDate: String, date: String, number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val lastLog = dbRepository.callLogDao.getLastEntryByCallerId(number)
            val inMillies = convertTimeStringToMillis(selectedDate)
            val getHourMin = convertMillisTo24HourFormat(inMillies)
            val data = Reminders(
                lastLog?.id ?: number.toLong(),
                getHourMin,
                date,
                callerId = number,
                0,
                true
            )
            dateTimeString = "$getHourMin:00 $date"
            setExactReminder(data)
        }
        Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show()
    }

    private suspend fun setExactReminder(data: Reminders) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    this,
                    exactAlarmPermission
                ) != PackageManager.PERMISSION_GRANTED

            /* ContextCompat.checkSelfPermission(
                 this,
                 useExactAlarmPermission
             ) != PackageManager.PERMISSION_GRANTED*/
            ) {
                Log.e("dliksfsd", "setAlarm: permission not granted")
                scheduleReminder(data)
                /*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                // Permissions already granted, proceed with scheduling the exact alarm
                scheduleReminder(data)
            }
        } else {
            // For versions below Android 12, no need to check runtime permissions
            scheduleReminder(data)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleReminder(data: Reminders) {
        // Cancel existing reminders for the same callerId
        val getCallerId = dbRepository.callLogDao.getCallLogById(callerId)
        if (getCallerId != null) {
            cancelExistingReminder(getCallerId.id.toString())
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        //   data.reminderId = getCallerId?.id?:data.reminderId
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(McsConstants.REMINDER, null, this, AlarmReceiver::class.java)
        intent.putExtra("alarmID", data.reminderId)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            this,
            (getCallerId?.id
                ?: 1).toInt(),  // Use callerId as the requestCode to identify the PendingIntent
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val heartbeatMsFor = McsConstants.ONE_MINUTE
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                reminderPendingIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                reminderPendingIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                reminderPendingIntent!!
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun NoteUI(callType: CallType) {
        val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
        /*        Dialog(
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    ), onDismissRequest = {
                        finish()
                    }) {*/
        Card {
            NotePopup(
                onDismiss = {
                    finish()
                },
                onSave = {
                    //val callType = intent?.getStringExtra("type")
                    CoroutineScope(Dispatchers.IO).launch {
                        val number = intent?.getStringExtra("number")
                        callLogHelper.insertRecentCallLogs {}
                        delay(100)
                        if (callType != CallType.PostCall) {
                            Log.e("hfhgh", "NoteUI: if ke ander")
                            val getTempData = dbRepository.tempData.getCallerIdOptions()
                            if (getTempData != null) {
                                getTempData.tempNotesMap[callerId] = it
                                dbRepository.tempData.insertOrUpdateCallerIdOptions(getTempData)
                            } else {
                                val createTempData = TempDataEntity(
                                    1,
                                    mutableMapOf(),
                                    mutableMapOf()
                                )
                                createTempData.tempNotesMap[callerId] = it
                                dbRepository.tempData.insertOrUpdateCallerIdOptions(createTempData)
                            }

                            finish()
                        } else {
                            Log.e("hfhgh", "NoteUI: else ke ander")
                            insertNoteOnCallLog(it, number!!)
                            delay(100)
                            finish()
                        }
                    }
                },
                onFocus = { },
                yourBringIntoViewRequester,
                onValueChange = {
                    //onNoteValueChange(it)
                }, ""
            )

        }
        // }

    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ShareContactUi(
        number: String, onDismiss: () -> Unit,
        onSave: () -> Unit,
    ) {
        val context = LocalContext.current
        val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ), onDismissRequest = {
                finish()
            }) {
            Card {
                ShareContact(
                    onDismiss = {
                        onDismiss()
                    },
                    onSave = {
                        context.openWhatsapp(number, it)
                        onSave()
                        //callLogNote(number, it)
                    },
                    onFocus = { },
                    yourBringIntoViewRequester,
                    onValueChange = {

                        //onNoteValueChange(it)
                    }, ""
                )

            }
        }

    }

    private suspend fun insertTasksOnCallLog(tasks: List<Tasks?>, callerId: String) {
        val tempTasks = arrayListOf<Tasks>()
        tasks.forEach {
            if (it != null && !it.tasks.isNullOrEmpty()) {
                tempTasks.add(it)
            }
        }
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(callerId)
            if (callLogs != null) {
                val dataToUpdate =
                    dbRepository.callLogDao.getCallLogById(callLogs.id)
                val tempData = dataToUpdate?.copy(tasks = tempTasks, isSynced = false)
                if (tempData != null) {
                    dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                }
            }
            syncUpdateCallLogs()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@PostCallActivity,
                    "Tasks saved successfully!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
            }


            /*  runOnUiThread {
                  Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                  // finish()
              }*/

        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@PostCallActivity, "Failed to save tasks!!", Toast.LENGTH_SHORT)
                    .show()
            }

            /*        runOnUiThread {Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()}      */

        }
    }


    private suspend fun insertNoteOnCallLog(note: String?, callerId: String) {
        callLogHelper.insertRecentCallLogs {}
        delay(100)
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(callerId)
            if (callLogs != null) {
                val dataToUpdate =
                    dbRepository.callLogDao.getCallLogById(callLogs.id)
                val tempData = dataToUpdate?.copy(callNote = note, isSynced = false)
                if (tempData != null) {
                    dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                }
            }
            syncUpdateCallLogs()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@PostCallActivity,
                    "Note saved successfully!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            /*  runOnUiThread {
                  Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                  // finish()
              }*/
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@PostCallActivity, "Failed to save note!", Toast.LENGTH_SHORT)
                    .show()
            }
            /*
             runOnUiThread {
                     Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
               }
            */
        }
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
        preNote: String = ""
    ) {
        val context = LocalContext.current
        //  val contactHelper = ContactHelper(context)

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
        Column(
            modifier = Modifier
                .background(Color(0xFFFFF2E0))
        ) {
            Column(
                modifier = Modifier
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

    @Composable
    fun AddLeadDialog(phone: String, onDismiss: () -> Unit, onSave: (name: String, status: String) -> Unit) {
        var name by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("New") }
        val stages = listOf("New", "Contacted", "Interested")
        val stageColors = mapOf(
            "New" to android.graphics.Color.parseColor("#607D8B"),
            "Contacted" to android.graphics.Color.parseColor("#1E88E5"),
            "Interested" to android.graphics.Color.parseColor("#7B1FA2")
        )
        Dialog(onDismissRequest = onDismiss) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF162032))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Lead", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                    }
                    Text(phone, color = androidx.compose.ui.graphics.Color(0xFF1E88E5), fontSize = 14.sp)
                    androidx.compose.material3.OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Name (optional)", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF1E2D40),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    Text("Initial Stage", color = Color.Gray, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        stages.forEach { s ->
                            val isSelected = status == s
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = { status = s },
                                label = { Text(s, fontSize = 11.sp) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Skip", color = Color.Gray)
                        }
                        androidx.compose.material3.Button(
                            onClick = { onSave(name, status) },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50.dp)
                        ) { Text("Save Lead", color = Color.White) }
                    }
                }
            }
        }
    }

    @Composable
    fun SaveContact(
        defaultName: String,
        numberFrom: Int,
        saveNumberInPhonebook: () -> Unit,
        saveNumberInApp: () -> Unit
    ) {
        val context = LocalContext.current



        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) {

                }, horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .background(color = Color.LightGray, shape = RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "Save Contact as ",
                    fontFamily = montserrat_semibold,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color.LightGray)
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
                                if (numberFrom == 1 || numberFrom == 3) Toast
                                    .makeText(
                                        context,
                                        "Number already saved",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show() else saveNumberInPhonebook()
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
                                if (numberFrom == 2 || numberFrom == 3) Toast
                                    .makeText(
                                        context,
                                        "Number already saved",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show() else saveNumberInApp()
                            },
                        color = if (numberFrom == 2 || numberFrom == 3) Color.Gray else ThemePurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }


    }



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotePopup(
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
    val noteText by remember {
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
        delay(300)
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
            .padding(10.dp)
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


