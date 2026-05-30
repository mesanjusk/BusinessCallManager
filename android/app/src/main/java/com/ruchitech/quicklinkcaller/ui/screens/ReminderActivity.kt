package com.ruchitech.quicklinkcaller.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.helper.formatTimestampToDate
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.ReminderUi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReminderActivity : ComponentActivity() {
    private var isBound: Boolean = false
    private var myService: CallerIdService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Cast the IBinder to your service binder
            val binder = service as CallerIdService.MyBinder
            // Get the service instance
            myService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            myService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, CallerIdService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        val data = intent.getStringExtra("reminder")
        val reminder = Gson().fromJson(data, Reminders::class.java)
        setContent {
            ReminderUi(reminder, onReminder = { hour, minutes, date ->
                myService?.hideUi?.value = false
                finish()
                val actualDate =
                    if (date == "Today") formatTimestampToDate(System.currentTimeMillis()) else date
                myService?.onReminder(hour, minutes, actualDate)
                Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show()
            }) {
                myService?.hideUi?.value = false
                finish()
            }
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setGravity(Gravity.CENTER)
        window.attributes.dimAmount = 0.7f
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

}