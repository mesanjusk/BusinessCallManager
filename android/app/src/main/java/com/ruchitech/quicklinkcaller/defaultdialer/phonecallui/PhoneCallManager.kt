package com.ruchitech.quicklinkcaller.defaultdialer.phonecallui

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import android.util.Log
import androidx.annotation.RequiresApi

class PhoneCallManager(private val context: Context) {

    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    companion object {
        @JvmField
        var call: Call? = null
    }

    /** 接听电话 */
    fun answer() {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    /** 断开电话（拒接或挂断） */
    fun disconnect() {
        call?.disconnect()
    }

    /** 打开免提 */
    fun setSpeaker(enabled: Boolean) {
       /* audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = enabled*/

        setAudioDevice()
    }
/*
    *//** 切换免提状态 *//*
    fun toggleSpeaker() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val newState = !audioManager.isSpeakerphoneOn
        audioManager.isSpeakerphoneOn = newState
        Log.e("PhoneCallManager", "Speaker new state: $newState")
    }*/

/*    fun toggleSpeaker() {
        PhoneCallManager.call?.let {
            val currentState = audioState?.route
            val newRoute = if (currentState == CallAudioState.ROUTE_SPEAKER) {
                CallAudioState.ROUTE_EARPIECE
            } else {
                CallAudioState.ROUTE_SPEAKER
            }
            setAudioRoute(newRoute)
        }
    }*/


    /** 静音控制 */
    fun setMute(mute: Boolean) {
        audioManager.isMicrophoneMute = mute
    }

    /** 暂停通话 */
    fun hold() {
        call?.hold()
    }

    /** 恢复通话 */
    fun unhold() {
        call?.unhold()
    }

    /** 交换通话 */
    fun swap() {
        call?.conferenceableCalls?.firstOrNull()?.let {
            call?.swapConference()
        }
    }

    /** 合并通话 */
    fun merge() {
        call?.conferenceableCalls?.firstOrNull()?.let {
            call?.conference(it)
        }
    }

    /** 注册回调 */
    fun registerCallback(onStateChanged: (Int) -> Unit) {
        call?.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                super.onStateChanged(call, state)
                onStateChanged(state)
                if (state == Call.STATE_ACTIVE) {
                    setAudioDevice()
                //    setSpeaker(true) // 激活时自动打开免提
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAudioDevice(){
        audioManager.mode = AudioManager.MODE_NORMAL
        val speakerDevice: AudioDeviceInfo? = getAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        Log.e("fdlkfgfdingufi", "setAudioDevice: ${speakerDevice?.audioProfiles}")

        Log.e("fdlkfgfdingufi", "setAudioDevice: ${audioManager.communicationDevice}")
        audioManager.availableCommunicationDevices.forEach {
            Log.e("fdlkfgfdingufi", "setAudioDevice: $it")
        }
        if (speakerDevice != null) {
            audioManager.setCommunicationDevice(speakerDevice)
        }
        audioManager.setCommunicationDevice(speakerDevice!!)
    }

    private fun getAudioDevice(type: Int): AudioDeviceInfo? {
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (deviceInfo in audioDevices) {
            if (type == deviceInfo.type) return deviceInfo
        }
        return null
    }

    /** 销毁资源 */
    fun destroy() {
        call = null
    }
}

