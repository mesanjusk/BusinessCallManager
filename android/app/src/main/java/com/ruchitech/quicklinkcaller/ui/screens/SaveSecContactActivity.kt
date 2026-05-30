package com.ruchitech.quicklinkcaller.ui.screens

import SaveContactUi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SaveSecContactActivity : ComponentActivity() {
    private var isBound: Boolean = false
    private var myService: CallerIdService? = null
    @Inject
    lateinit var contactHelper: ContactHelper
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
        val number = intent.getStringExtra("number")
        openKeyboardWithoutFocus()
        setContent {
/*            TextField(value = "gfkhg", onValueChange = {})*/
                        SaveContactUi(number, onClose = {
                            myService?.hideUi?.value = false
                            finish()
                        }, onSave = { name, number ->
                            myService?.hideUi?.value = false
                            myService?.saveContactInApp(name, number, "")
                            finish()
                        }, onFocusChangesForName = {}, contactHelper = contactHelper)
        }

        /*        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawableResource(android.R.color.transparent)
                window.setGravity(Gravity.CENTER)
                window.attributes.dimAmount = 0.7f
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)*/
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

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
