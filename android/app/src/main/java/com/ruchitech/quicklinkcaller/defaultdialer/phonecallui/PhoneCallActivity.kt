package com.ruchitech.quicklinkcaller.defaultdialer.phonecallui

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.defaultdialer.ActivityStack
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask

class AudioDeviceHelper(private val context: Context) {
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun enableSpeakerphone(): Boolean {
        return setCommunicationDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun disableSpeakerphone(): Boolean {
        return try {
            audioManager.clearCommunicationDevice()
            true
        } catch (e: Exception) {
            Log.e("AudioDevice", "Failed to clear communication device", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setCommunicationDevice(deviceType: Int): Boolean {
        return try {
            val devices = audioManager.getAvailableCommunicationDevices()
            val targetDevice = devices.firstOrNull { it.type == deviceType }

            targetDevice?.let {
                audioManager.setCommunicationDevice(it)
                true
            } ?: run {
                Log.w("AudioDevice", "Requested device type $deviceType not available")
                false
            }
        } catch (e: Exception) {
            Log.e("AudioDevice", "Error setting communication device", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getCurrentAudioDevice(): AudioDeviceInfo? {
        return try {
            audioManager.communicationDevice
        } catch (e: Exception) {
            Log.e("AudioDevice", "Error getting current device", e)
            null
        }
    }
}

@AndroidEntryPoint
class PhoneCallActivity : ComponentActivity() {

    private var phoneCallManager: PhoneCallManager? = null
    private var callType: PhoneCallService.CallType? = null
    private var phoneNumber: String? = null
    private var isMuted by mutableStateOf(false)
    private var callingTime by mutableStateOf(0)
    private var onGoingCallTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityStack.getInstance().addActivity(this)
        phoneCallManager = PhoneCallManager(this)
        val audioHelper = AudioDeviceHelper(this)
        onGoingCallTimer = Timer()
        if (intent != null) {
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            callType =
                intent.getSerializableExtra(Intent.EXTRA_MIME_TYPES) as PhoneCallService.CallType?
        }

        showOnLockScreen()
        //   startCallTimer()
        setContent {
            var callState by remember { mutableStateOf(Call.STATE_NEW) }
            var isSpeakerOn by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                phoneCallManager?.registerCallback { state ->
                    callState = state
                    if (state == Call.STATE_ACTIVE) {
                        startCallTimer()
                        phoneCallManager?.answer()
                    }
                    if (state == Call.STATE_DISCONNECTED) stopTimer()
                }
            }
            PhoneCallScreen(
                callType = callType,
                phoneNumber = phoneNumber ?: "",
                callingTime = callingTime,
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                onPickUp = {
                    phoneCallManager?.answer()
                    startCallTimer()
                },
                onHangUp = {
                    phoneCallManager?.disconnect()
                    stopTimer()
                },
                onToggleMute = {
                    isMuted = !isMuted
                    phoneCallManager?.setMute(isMuted)
                },
                onHold = {
                    phoneCallManager?.hold()
                },
                onUnhold = {
                    phoneCallManager?.unhold()
                },
                onSwap = {
                    phoneCallManager?.swap()
                },
                onMerge = {
                    phoneCallManager?.merge()
                },
                onToggleSpeaker = {
                    isSpeakerOn = !isSpeakerOn
                    phoneCallManager?.setSpeaker(true)
                }
            )
        }
    }

    private fun startCallTimer() {
        onGoingCallTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    Log.e("gjfhgjfhgjfgn", "run: $callingTime")
                    callingTime++
                }
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        onGoingCallTimer?.cancel()
        callingTime = 0
    }

    fun showOnLockScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        phoneCallManager?.destroy()
    }

    companion object {
        @JvmStatic
        fun actionStart(
            context: Context, phoneNumber: String?,
            callType: PhoneCallService.CallType?
        ) {
            val intent = Intent(context, PhoneCallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_MIME_TYPES, callType)
            intent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber)
            context.startActivity(intent)
        }
    }
}

@Composable
fun PhoneCallScreen(
    callType: PhoneCallService.CallType?,
    phoneNumber: String,
    callingTime: Int,
    isSpeakerOn: Boolean,
    isMuted: Boolean,
    onPickUp: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onHold: () -> Unit,
    onUnhold: () -> Unit,
    onMerge: () -> Unit,
    onSwap: () -> Unit,
    onToggleSpeaker: () -> Unit,
) {
    val isIncoming = callType == PhoneCallService.CallType.CALL_IN
    val timeFormatted = String.format("%02d:%02d", callingTime / 60, callingTime % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101010), Color(0xFF1E1E1E))))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Call Type
            Text(
                text = if (isIncoming) "Incoming Call" else "Outgoing Call",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Phone Number
            Text(
                text = phoneNumber,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // Duration
            if (callingTime > 0) {
                Text(
                    text = "Duration: $timeFormatted",
                    color = Color(0xFF4CAF50),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Row for Mute / Hold / Merge / Swap
            if (callingTime > 0) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(70.dp)
                            .background(if (isMuted) Color.Gray else Color.DarkGray, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
                            ),
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                if (isSpeakerOn) Color.Gray else Color.DarkGray,
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_batteryopt_black),
                            contentDescription = "Speaker",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }


                    IconButton(
                        onClick = onHold,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "Hold",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onUnhold,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Unhold",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onMerge,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_merge),
                            contentDescription = "Merge",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_swap_calls),
                            contentDescription = "Swap",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Pickup / Hangup Row
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isIncoming) {
                    IconButton(
                        onClick = onPickUp,
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color(0xFF4CAF50), shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_call_quick),
                            contentDescription = "Pick Up",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onHangUp,
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFF44336), shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_call_quick),
                        contentDescription = "Hang Up",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
