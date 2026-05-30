package com.ruchitech.quicklinkcaller

// MyApp.kt
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.syncCallLogs
import com.ruchitech.quicklinkcaller.persistence.recievers.TriggerReceiver
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.test1.core.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {
    lateinit var launcherActivity: MainActivity

    @Inject
    lateinit var pref: AppPreference

    @Inject
    lateinit var appPreference: AppPreference

    @Inject
    lateinit var dbRepository: DbRepository

    @Inject
    lateinit var callLogHelper: CallLogHelper

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    var tempNote = ""
    var savedNote: String = ""
    var savedNumber: String = ""

    companion object {
        lateinit var instance: MyApp
            private set
        var isAppInForeground = false
        var needDataReload: Boolean = false
    }

    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, Configuration.Builder().build())
        notificationHelper = NotificationHelper(this)
        registerForgreoundCallBack()
        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        TriggerReceiver.register(this)
        CoroutineScope(Dispatchers.IO).launch {
            val currentTime = java.util.Date().time
            val notificationsList = dbRepository.tempData.getExpiredNotifications(currentTime)
            notificationsList.forEach { notifications ->
                NotificationManagerCompat.from(this@MyApp)
                    .cancel(notifications.notificationId)
            }
            dbRepository.tempData.cancelExpiredNotifications(currentTime)
        }
    }

    fun updateCallLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            callLogHelper.insertRecentCallLogs {
                syncCallLogs()
            }
        }
    }

    private fun registerForgreoundCallBack() {

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Activity created
            }

            override fun onActivityStarted(activity: Activity) {
                // Activity started
                isAppInForeground = true
            }

            override fun onActivityResumed(activity: Activity) {
                // Activity resumed
                isAppInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                // Activity paused
                isAppInForeground = false
            }

            override fun onActivityStopped(activity: Activity) {
                // Activity stopped
                isAppInForeground = false
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Activity state saved
            }

            override fun onActivityDestroyed(activity: Activity) {
                // Activity destroyed
            }
        })
    }


}