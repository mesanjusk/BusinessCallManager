package com.ruchitech.quicklinkcaller.defaultdialer.phonecallui

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.defaultdialer.ActivityStack
import com.ruchitech.quicklinkcaller.defaultdialer.phonecallui.PhoneCallActivity.Companion.actionStart

class PhoneCallService : InCallService() {

    private val callback: Call.Callback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            super.onStateChanged(call, state)

            when (state) {
                Call.STATE_ACTIVE -> {
                    // Call is active
                }
                Call.STATE_DISCONNECTED -> {
                    ActivityStack.getInstance()
                        .finishActivity(PhoneCallActivity::class.java)
                }
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        call.registerCallback(callback)
        PhoneCallManager.call = call

        val callType = when (call.state) {
            Call.STATE_RINGING -> CallType.CALL_IN
            Call.STATE_CONNECTING, Call.STATE_DIALING -> CallType.CALL_OUT
            else -> null
        }

        callType?.let {
            val details = call.details
            val phoneNumber = details.handle.schemeSpecificPart
            actionStart(this, phoneNumber, it)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
        PhoneCallManager.call = null
    }



    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)

        // Log the current audio route
        Log.d("CallAudio", "Audio route changed to: ${audioState.route}")

        // Convert route to human-readable string
        val routeName = when (audioState.route) {
            CallAudioState.ROUTE_EARPIECE -> "Earpiece"
            CallAudioState.ROUTE_BLUETOOTH -> "Bluetooth"
            CallAudioState.ROUTE_WIRED_HEADSET -> "Wired Headset"
            CallAudioState.ROUTE_SPEAKER -> "Speaker"
            else -> "Unknown (${audioState.route})"
        }
        Log.d("CallAudio", "Current audio route: $routeName")

        // Handle specific route changes
        when (audioState.route) {
            CallAudioState.ROUTE_SPEAKER -> {
                // Speakerphone is active
                handleSpeakerphoneState(true)
            }
            CallAudioState.ROUTE_EARPIECE -> {
                // Speakerphone is off
                handleSpeakerphoneState(false)
            }
        }

        // Update UI based on audio state
      //updateAudioRouteUI(audioState)
    }

    private fun handleSpeakerphoneState(isSpeakerOn: Boolean) {
        // Your speakerphone handling logic
        Log.d("SpeakerState", "Speakerphone is ${if (isSpeakerOn) "ON" else "OFF"}")

        // Example: Update a speakerphone toggle button state
     //   binding?.speakerButton?.isChecked = isSpeakerOn
    }

    fun toggleSpeakerphone() {
        val currentRoute = callAudioState.route
        val newRoute = if (currentRoute == CallAudioState.ROUTE_SPEAKER) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        setAudioRoute(newRoute)
    }

    enum class CallType {
        CALL_IN, CALL_OUT
    }
}
