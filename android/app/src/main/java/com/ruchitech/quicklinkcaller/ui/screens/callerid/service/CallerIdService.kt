package com.ruchitech.quicklinkcaller.ui.screens.callerid.service

import SaveContactUi
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.CallType
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.extractInitials
import com.ruchitech.quicklinkcaller.helper.formatDateFromMillis
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.helper.shareContact
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.recievers.AlarmReceiver
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.PostCallInfoPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularInitialsButton
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularInitialsButton2
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CallerIdService : LifecycleService(), SavedStateRegistryOwner {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var callType: CallType = CallType.IncomingCall
    private var phoneNo = ""
    private val ONE: Int by lazy { 1 }
    private val TWO: Int by lazy { 2 }
    private val ZERO: Int by lazy { 0 }
    private var contentView: View? = null
    private lateinit var windowManager: WindowManager

    // Track the initial touch position
    private var initialX = 0
    private var initialY = 0

    // Track the position of the window
    private var windowLayoutParams: WindowManager.LayoutParams? = null

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
    private var numberByIntent: String? = null
    private var activeCalls: MutableMap<String, Int> = mutableMapOf()
    private var activeCallNotes: MutableMap<String, String> = mutableMapOf()
    private var activeCallTasks: MutableMap<String, List<Tasks?>> = mutableMapOf()
    private var activeCallsName: MutableMap<String, String> = mutableMapOf()
    private val binder = MyBinder()
    var hideUi = mutableStateOf(false)
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var isTimerRunning = false
    private val debounceInterval = 500L // 1 second
    private var lastEventTime: Long = 0
    private val handlerToStopForeground: Handler by lazy { Handler() }

    inner class MyBinder : Binder() {
        fun getService(): CallerIdService = this@CallerIdService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    fun doSomething() {
        Log.e("jhgfghgfhf", "doSomething: working")
        Toast.makeText(this, "this is test", Toast.LENGTH_SHORT).show()
    }

    private fun startTimer(onSuccess: () -> Unit) {
        isTimerRunning = true
        runnable = Runnable {
            onSuccess()
            stopTimer()
        }
        handler.postDelayed(runnable, 5000L)
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(runnable)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundService {}
        MyApp.instance.tempNote = ""
        callType = when (intent?.getIntExtra("callType", -1) ?: -1) {
            ONE -> CallType.IncomingCall
            TWO -> CallType.OutgoingCall
            ZERO -> CallType.PostCall
            else -> CallType.IncomingCall
        }
        phoneNo = intent?.getStringExtra("phoneNo") ?: ""
        callerId = intent?.getStringExtra("phoneNo") ?: ""
        numberByIntent = intent?.getStringExtra("phoneNo")
        Log.e("fgihgiofghfiogj", "onStartCommand: $callType, $callerId")
        val calls = intent?.getStringExtra("activeCalls")
        val activeCallNotesJson = intent?.getStringExtra("activeCallNote")
        val activeCallTasksJson = intent?.getStringExtra("activeCallTasks")
        val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
        activeCalls = Gson().fromJson(calls, mapType)

        val mapType2 = object : TypeToken<MutableMap<String, String>>() {}.type
        activeCallNotes = Gson().fromJson(activeCallNotesJson, mapType2)

        val mapType3 = object : TypeToken<MutableMap<String, List<Tasks?>>>() {}.type
        activeCallTasks = Gson().fromJson(activeCallTasksJson, mapType3)

        super.onStartCommand(intent, flags, startId)
        CoroutineScope(Dispatchers.IO).launch {
            callLogHelper.insertRecentCallLogs {}
        }
        if (contentView != null) {
            if (contentView?.isAttachedToWindow == true) {
                windowManager.removeViewImmediate(contentView)
                contentView = null
            }
        }

        val clickUi = mutableStateOf(false)

        if (contentView == null) {
            contentView = ComposeView(this).apply {
                setViewTreeSavedStateRegistryOwner(this@CallerIdService)
                setContent {
                    var showDialog by remember { mutableStateOf(callType == CallType.PostCall) }
                    var showSemiUi by remember {
                        mutableStateOf(true)
                    }
                    var saveInAppDialog by remember {
                        mutableStateOf(false)
                    }
                    var number by remember {
                        mutableStateOf(numberByIntent)
                    }
                    val animatedAlpha by animateFloatAsState(if (showDialog) 1f else 0f, label = "")
                    var colorForName by remember { mutableStateOf(dimBlack) }
                    var numberFrom by remember {
                        mutableIntStateOf(0)  //1 for phonebook, 2 in-app book, 3 for both side saved // 0 unknown
                    }

                    val scope = rememberCoroutineScope()

                    var contactName by remember { mutableStateOf("") }
                    DisposableEffect(true) {
                        startTimer {
                            showSemiUi = false
                        }
                        onDispose {
                        }
                    }
                    DisposableEffect(number) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            var checkName: String? = ""
                            val contactDetails =
                                contactHelper.getNameFromPhoneNumber(number!!) /// ContactHelper(context).getContactDetailsByPhoneNumber(number)
                            if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                                checkName =
                                    dbRepository.contact.getContactByPhoneNumber(number!!)?.contact_title
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
                                    dbRepository.contact.getContactByPhoneNumber(number!!)?.contact_title
                                if (!alsoCheckInAppDB.isNullOrEmpty()) {
                                    numberFrom = 3
                                }
                                contactName = contactDetails.ifEmpty { "Unknown" }

                            }
                        }
                        onDispose {
                            job.cancel()
                        }
                    }



                    if (!showDialog) {
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(15.dp)
                        ) {
                            if (activeCalls.size > 1) {
                                runBlocking {
                                    getNameForList()
                                    delay(300)
                                }
                                /* LazyRow(modifier = Modifier.align(Alignment.TopEnd)) {
                                     itemsIndexed(activeCalls.toList()) { index, item ->
                                         val isActive = number == item.first
                                         CircularInitialsButton(
                                             isActive,
                                             initials = extractInitials(
                                                 activeCallsName[item.first]
                                             )
                                         ) {
                                             number = item.first
                                         }
                                         Spacer(modifier = Modifier.width(8.dp))
                                     }
                                 }*/
                            }

                            showDialog = clickUi.value

                            Row(
                                modifier = Modifier
                                    .background(Color.Transparent)
                                    .padding(0.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val interactionSource =
                                    remember { MutableInteractionSource() }
                                val count =
                                    if (activeCalls.isEmpty()) "1" else activeCalls.size.toString()
                                CircularInitialsButton2(
                                    false,
                                    initials = count
                                ) {
                                    showDialog = true
                                }
                                Spacer(modifier = Modifier.width(0.dp))
                                if (showSemiUi) {
                                    Box(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .padding(start = 5.dp)
                                            .background(Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7F)
                                                .padding(0.dp)
                                                .background(
                                                    Color.White,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .align(Alignment.CenterStart),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            /*    Icon(
                                                    painter = painterResource(id = R.drawable.drag),
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(35.dp),
                                                )
    */
                                            Column(
                                                modifier = Modifier
                                                    .height(45.dp)
                                                    .padding(top = 0.dp, start = 0.dp, end = 10.dp)
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = interactionSource
                                                    ) {
                                                        showDialog = true
                                                        updateView(true)
                                                    },
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = contactName,
                                                    lineHeight = 0.sp,
                                                    modifier = Modifier.padding(
                                                        start = 10.dp,
                                                        top = 0.dp,
                                                        end = 10.dp
                                                    ),
                                                    maxLines = 1,
                                                    fontSize = 14.sp.nonScaledSp,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(
                                                        color = colorForName,
                                                        fontFamily = montserrat_semibold
                                                    )
                                                )
                                                Text(
                                                    text = number!!,
                                                    modifier = Modifier.padding(start = 10.dp),
                                                    maxLines = 1,
                                                    fontSize = 12.sp,
                                                    lineHeight = 0.sp,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(
                                                        color = Color.Gray,
                                                        fontFamily = FontFamily.SansSerif,
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (showDialog) {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .alpha(animatedAlpha)
                                .graphicsLayer {
                                    scaleX = animatedAlpha
                                    scaleY = animatedAlpha
                                },
                            shadowElevation = 0.dp
                        ) {
                            if (!hideUi.value) {
                                Column(modifier = Modifier.background(color = Color.Transparent)) {
                                    if (activeCalls.size > 1) {
                                        runBlocking {
                                            getNameForList()
                                            delay(100)
                                        }
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(end = 14.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            itemsIndexed(activeCalls.toList()) { _, item ->
                                                val isActive = number == item.first
                                                CircularInitialsButton(
                                                    isActive = isActive,
                                                    initials = extractInitials(
                                                        activeCallsName[item.first]
                                                    )
                                                ) {
                                                    number = item.first
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                        }
                                    }

                                    PostCallInfoPopup(
                                        dbRepository,
                                        number = number ?: "",
                                        openWhatsapp = {
                                            openWhatsapp(it)
                                        },
                                        openTextMsg = {
                                            number?.let { sendTextMessage(it) }
                                        },
                                        callBack = {
                                            number?.let { makePhoneCall(it) }
                                        },
                                        saveNumberInApp = {
                                            hideUi.value = true
                                            saveInAppDialog = true
                                        },
                                        saveNumberInPhonebook = {
                                            stopAppCallerIdService(this@CallerIdService)
                                            if (number != null) {
                                                saveNumberToContacts(number!!, "")
                                            }
                                        },
                                        toastMsg = {
                                            Toast.makeText(
                                                this@CallerIdService,
                                                it,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        callLogNote = { number, note ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                //      val id = callLogHelper.getCallLogIdByPhoneNumber(number)
                                                callLogHelper.insertRecentCallLogs {}
                                                delay(100)
                                                if (callType == CallType.OutgoingCall) {
                                                    Log.e(
                                                        "kmijuhyg",
                                                        "onStartCommand: $number $note"
                                                    )
                                                    MyApp.instance.savedNote = note
                                                    MyApp.instance.savedNumber = number
                                                } else {
                                                    insertNoteOnCallLog(note, number)
                                                }
                                                //   finish()
                                            }
                                        },
                                        onReminder = { mergeDate, date ->

                                            number?.let {
                                                setAlarm(mergeDate, date, it)
                                            }
                                        },
                                        onClose = {
                                            windowManager.removeViewImmediate(contentView)
                                            contentView = null
                                            MyApp.instance.tempNote = ""
                                            sendClearDataToPersistence()
                                            activeCalls.clear()
                                            activeCallsName.clear()
                                            stopAppCallerIdService(this@CallerIdService)
                                        },
                                        onHideUi = {
                                            hideUi.value = it
                                        },
                                        onFocusChangesForNote = {
                                            if (it) {
                                                Handler().postDelayed({
                                                    openKeyboardWithoutFocus()
                                                }, 1000)
                                            }
                                            updateView(it)
                                        },
                                        onMinimize = {
                                            showSemiUi = false
                                            showDialog = false
                                            clickUi.value = false
                                            updateView(false)
                                        },
                                        onNumberSwitch = {
                                        },
                                        notesList = activeCallNotes,
                                        onNoteValueChange = {
                                            activeCallNotes[number ?: ""] = it
                                            val intent =
                                                Intent("com.example.ACTION_CALL_STATE_CHANGED")
                                            intent.putExtra("clearData", false)
                                            intent.putExtra("number", number)
                                            intent.putExtra("note", it)
                                            intent.putExtra("notes", true)
                                            context.sendBroadcast(intent)
                                        },
                                        onShareContact = { name, number ->
                                            shareContact(name ?: "", number)
                                        },
                                        callType = callType,
                                        callLogHelper = callLogHelper,
                                        tasksList = activeCallTasks,
                                        onTasksValueChange = {
                                            Log.e("kijuhggy", "onStartCommand: $it")
                                            activeCallTasks[number ?: ""] = it
                                            val intent =
                                                Intent("com.example.ACTION_CALL_STATE_CHANGED")
                                            intent.putExtra("clearData", false)
                                            intent.putExtra("number", number)
                                            intent.putExtra("tasksList", Gson().toJson(it))
                                            intent.putExtra("tasks", true)
                                            context.sendBroadcast(intent)
                                        },
                                        onTasksSave = {
                                            scope.launch {
                                                number?.let { it1 ->
                                                    insertTasksOnCallLog(
                                                        it,
                                                        it1
                                                    )
                                                }
                                            }
                                        },
                                        defaultType = appPreference.defaultTabForNoteTasks,
                                        onNoteTabChange = {
                                            appPreference.defaultTabForNoteTasks = it
                                        }, contactHelper = contactHelper)

                                }
                            }
                            if (saveInAppDialog) {
                                SaveContactUi(number, "", onClose = {
                                    saveInAppDialog = false
                                    hideUi.value = false
                                }, { name, number ->
                                    saveInAppDialog = false
                                    contactName = name
                                    saveContactInApp(name, number, "")
                                    hideUi.value = false
                                    Toast.makeText(
                                        this@CallerIdService,
                                        "Number saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }, onFocusChangesForName = {
                                    if (it) {
                                        Handler().postDelayed({
                                            openKeyboardWithoutFocus()
                                        }, 1000)
                                    }
                                    updateView(it)
                                }, callingByService = true,contactHelper)
                            }
                        }

                    }
                }

            }



            contentView?.setViewTreeLifecycleOwner(this)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.x = appPreference.initialX
            params.y = appPreference.initialY

            windowManager.addView(contentView, params)
            if (callType == CallType.PostCall && MyApp.instance.savedNote.isNotEmpty() && MyApp.instance.savedNumber.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    callLogHelper.insertRecentCallLogs {
                    }
                    delay(1000)
                    insertNoteOnCallLog(MyApp.instance.savedNote, MyApp.instance.savedNumber)
                    MyApp.instance.savedNote = ""
                    MyApp.instance.savedNumber = ""
                }
            }
            //handleBackButton()
            windowLayoutParams = contentView?.layoutParams as WindowManager.LayoutParams?
            // Set touch listener to handle movement
            var actionInt = 1
            val MIN_MOVE_DISTANCE = 10 // Set your minimum move distance threshold here

            var lastTapTime: Long = 0
            var lastX: Float = 0f
            var lastY: Float = 0f
            val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

            contentView?.apply {
                setOnTouchListener { view, event ->
                    Log.e("jhbgvfcx", "onStartCommand: $touchSlop")
                    actionInt = event.action
                    val currentTime = System.currentTimeMillis()
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = event.rawX.toInt() - windowLayoutParams?.x!!
                            initialY = event.rawY.toInt() - windowLayoutParams?.y!!
                            lastX = (event.rawX.toInt() - windowLayoutParams?.x!!).toFloat()
                            lastY = (event.rawY.toInt() - windowLayoutParams?.y!!).toFloat()
                            false
                        }

                        MotionEvent.ACTION_MOVE -> {
                            // Calculate the new position of the window based on touch event
                            val deltaX = (event.rawX - initialX).toInt()
                            val deltaY = (event.rawY - initialY).toInt()
                            // Handle movement

                            val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                            //   Toast.makeText(this@CallerIdService, "$touchSlop >  $distance", Toast.LENGTH_SHORT).show()
                            Log.e("kijuhygftr", "onStartCommand: $distance")
                            if (distance > touchSlop) {
                                val newDeltaX = (event.rawX - initialX).toInt()
                                val newDeltaY = (event.rawY - initialY).toInt()
                                // Update the position of the window
                                windowLayoutParams?.x = newDeltaX
                                windowLayoutParams?.y = newDeltaY
                                appPreference.initialX = deltaX
                                appPreference.initialY = deltaY
                                // Update the window layout
                                lastEventTime = currentTime
                                windowManager.updateViewLayout(contentView, windowLayoutParams)
                            }/*else{
                                clickUi.value = true
                            }*/
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            // Reset last tap time
                            lastTapTime = 0
                            lastX = (event.rawX.toInt() - windowLayoutParams?.x!!).toFloat()
                            lastY = (event.rawY.toInt() - windowLayoutParams?.y!!).toFloat()
                            false
                        }

                        else -> {
                            /*       if (actionInt == 0 && !clickUi.value) {
                                                clickUi.value = true
                                                actionInt =1
                                                view.performClick()
                                       view.performClick()
                                   }*/
                            false
                        }
                    }

                }
                /*     setOnClickListener {
                         val currentTime = System.currentTimeMillis()
                         val timeDifference = currentTime - lastEventTime
                         if (timeDifference >= debounceInterval) {
                             if (!clickUi.value) {
                                   clickUi.value = true
                             }
                             //  actionInt = 0
                             lastEventTime = currentTime
                         }
                     }*/
            }


        } else {
            Log.e("kjhuygtf", "contentView is already initialized")
        }

        /*        stopSelf()
                val popupIntent =
                    Intent(this@CallerIdService, PostCallActivity::class.java)
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                popupIntent.putExtra("number", phoneNo)
                startActivity(popupIntent)*/
        return START_NOT_STICKY
    }


    fun sendClearDataToPersistence() {
        val intent = Intent("com.example.ACTION_CALL_STATE_CHANGED")
        intent.putExtra("clearData", true)
        sendBroadcast(intent)
    }

    fun sendNoteDataToPersistence() {
        val intent = Intent("com.example.ACTION_CALL_STATE_CHANGED")
        intent.putExtra("clearData", false)
        intent.putExtra("number", "")
        intent.putExtra("note", "")
        sendBroadcast(intent)
    }

    fun getNameForList() {
        CoroutineScope(Dispatchers.IO).launch {
            activeCalls.forEach { (t, u) ->
                var contactName = ""
                var checkName: String? = ""
                val contactDetails =
                    contactHelper.getNameFromPhoneNumber(t) /// ContactHelper(context).getContactDetailsByPhoneNumber(number)
                if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                    checkName =
                        dbRepository.contact.getContactByPhoneNumber(t)?.contact_title
                    contactName = if (!checkName.isNullOrEmpty()) {
                        checkName
                    } else "Unknown"
                    activeCallsName[t] = contactName
                } else {
                    val alsoCheckInAppDB =
                        dbRepository.contact.getContactByPhoneNumber(t)?.contact_title
                    contactName = contactDetails.ifEmpty { "Unknown" }
                    activeCallsName[t] = contactName

                }
            }
        }

    }

    private fun updateView(isUiVisible: Boolean) {
        windowLayoutParams = contentView?.layoutParams as WindowManager.LayoutParams?
        if (isUiVisible) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            initialX = windowLayoutParams?.x!!
            initialY = windowLayoutParams?.y!!
            windowLayoutParams = params
            windowLayoutParams?.x = initialX
            windowLayoutParams?.y = initialY
            windowManager.updateViewLayout(contentView, windowLayoutParams)

        } else {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            initialX = windowLayoutParams?.x!!
            initialY = windowLayoutParams?.y!!
            windowLayoutParams = params
            windowLayoutParams?.x = initialX
            windowLayoutParams?.y = initialY
            windowManager.updateViewLayout(contentView, windowLayoutParams)
        }

    }

    fun updateFlags(isFocused: Boolean) {
        if (isFocused) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            windowManager.updateViewLayout(contentView, params)
            openKeyboardWithoutFocus()
        } else {
            hideKeyboard()
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            windowManager.updateViewLayout(contentView, params)
        }
    }

    fun openKeyboardWithoutFocus() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun handleBackButton() {

        val callback = View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                stopAppCallerIdService(this)
                return@OnKeyListener true // Consume the event
            }
            false
        }

        this.apply {
            val view = contentView
            view?.isFocusable = true
            view?.isFocusableInTouchMode = true
            view?.requestFocus()
            view?.setOnKeyListener(callback)
        }
    }

    fun onReminder(hours: String, minutes: String, date: String) {
        numberByIntent?.let { setAlarm(hours, minutes, date) }
    }


    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
        if (contentView?.isAttachedToWindow == true) {
            windowManager.removeViewImmediate(contentView)
            contentView = null
        }
    }

    private suspend fun insertNoteOnCallLog(note: String?, callerId: String) {
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
                Toast.makeText(this@CallerIdService, "Note saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }

            /*  runOnUiThread {
                  Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                  // finish()
              }*/
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@CallerIdService, "Failed to save note!", Toast.LENGTH_SHORT)
                    .show()
            }
            /*        runOnUiThread {
                        Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
                    }*/
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
                    this@CallerIdService,
                    "Tasks saved successfully!",
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
                Toast.makeText(this@CallerIdService, "Failed to save note!", Toast.LENGTH_SHORT)
                    .show()
            }
            /*        runOnUiThread {
                        Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
                    }*/
        }
    }


    fun saveContactInApp(name: String, number: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(
                Contact(
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

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}

/*
* https://www.notion.so/invite/c6b69c572bf5f7fc2bf8e33276b432568e4b7f3a
* */
