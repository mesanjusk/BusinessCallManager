package com.ruchitech.quicklinkcaller.helper

import android.content.Context
import android.content.SharedPreferences
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppPreference
@Inject
constructor(
    @ApplicationContext private val context: Context
) {


    private fun getSharedPreference(preferenceFile: String): SharedPreferences {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
    }

    private fun getSharedPreferenceEditor(
        preferenceFile: String,
    ): SharedPreferences.Editor {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE).edit()
    }

    fun clearData() {
        getSharedPreferenceEditor(PREFERENCE_KEY).clear().commit()
    }

    var lastHearBeatTime: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(LAST_EXECUTION_TIME_KEY, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            LAST_EXECUTION_TIME_KEY,
            value
        ).apply()


    var lastCase44TriggerTime: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(LAST_CASE_44_TRIGGER_TIME, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            LAST_CASE_44_TRIGGER_TIME,
            value
        ).apply()

    var defaultTabForNoteTasks: Int
        get() = getSharedPreference(PREFERENCE_KEY).getInt(DEFAULT_TAB, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putInt(
            DEFAULT_TAB,
            value
        ).apply()

    var userId: String?
        get() = getSharedPreference(PREFERENCE_KEY).getString(USER_ID, null)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putString(
            USER_ID,
            value
        ).apply()


    var mobileNumber: String?
        get() = getSharedPreference(PREFERENCE_KEY).getString(MOBILE_NUMBER, null)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putString(
            MOBILE_NUMBER,
            value
        ).apply()


    var initialX: Int
        get() = getSharedPreference(PREFERENCE_KEY).getInt(INIT_X, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putInt(
            INIT_X,
            value
        ).apply()
    var initialY: Int
        get() = getSharedPreference(PREFERENCE_KEY).getInt(INIT_Y, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putInt(
            INIT_Y,
            value
        ).apply()

    var callerIdType: Int  // 0 for popup 1 for notification
        get() = getSharedPreference(PREFERENCE_KEY).getInt(CALLER_ID_TYPE, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putInt(
            CALLER_ID_TYPE,
            value
        ).apply()


    var isInitialCallLogDone: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(INITIAL_CALL_LOGS_DONE, false)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            INITIAL_CALL_LOGS_DONE,
            value
        ).apply()


    var oneTimeSyncNoteShow: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(ONE_TIME_SHOWN_SYNC_WARNING_ALERT, true)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            ONE_TIME_SHOWN_SYNC_WARNING_ALERT,
            value
        ).apply()

/*    var updateLastInitializationTimestamp: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(UpdateLastInitializationTimestamp, 0L)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            UpdateLastInitializationTimestamp,
            value
        ).apply()*/

    var shouldForeground: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(SHOULD_FOREGROUND, false)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            SHOULD_FOREGROUND,
            value
        ).apply()

    var isFirstOpen: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(IS_FIRST_OPEN, true)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            IS_FIRST_OPEN,
            value
        ).apply()

    var contactSynced: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(CONTACT_SYNCED, true)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            CONTACT_SYNCED,
            value
        ).apply()


    var setDefaultDone: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(SET_DFAULT_DONE, false)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            SET_DFAULT_DONE,
            value
        ).apply()

    var geminiApiKey: String?
        get() = getSharedPreference(PREFERENCE_KEY).getString(GEMINI_API_KEY, null)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putString(GEMINI_API_KEY, value).apply()

    var googleAccountEmail: String?
        get() = getSharedPreference(PREFERENCE_KEY).getString(GOOGLE_ACCOUNT_EMAIL, null)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putString(GOOGLE_ACCOUNT_EMAIL, value).apply()

    var personalNumbers: Set<String>
        get() = getSharedPreference(PREFERENCE_KEY).getStringSet(PERSONAL_NUMBERS, emptySet()) ?: emptySet()
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putStringSet(PERSONAL_NUMBERS, value).apply()

    // SharedPreferences extension functions
    fun SharedPreferences.getCallerIdOptions(key: String): Set<AllCallerIdOptions> {
        val optionsString = getString(key, null)
        return optionsString?.split(",")?.map { AllCallerIdOptions.valueOf(it) }?.toSet() ?: emptySet()
    }

    fun SharedPreferences.Editor.putCallerIdOptions(key: String, options: Set<AllCallerIdOptions>) {
        val optionsString = options.joinToString(",") { it.name }
        putString(key, optionsString).commit()
    }

    // Usage in your ViewModel or wherever you need to store/retrieve the options
    var callerIdOptions: Set<AllCallerIdOptions>
        get() = getSharedPreference(PREFERENCE_KEY).getCallerIdOptions(CALLER_ID_OPTIONS)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putCallerIdOptions(CALLER_ID_OPTIONS, value)


    companion object {
        const val PREFERENCE_KEY = "turn_out_pref"
        private const val LAST_EXECUTION_TIME_KEY = "LAST_EXECUTION_TIME_KEY "
        private const val LAST_CASE_44_TRIGGER_TIME = "lastCase44TriggerTime "
        private const val DEFAULT_TAB = "defaultTab "
        private const val INITIAL_CALL_LOGS_DONE = "INITIAL_CALL_LOGS_DONE "
        private const val ONE_TIME_SHOWN_SYNC_WARNING_ALERT = "ONE_TIME_SHOWN_SYNC_WARNING_ALERT "
        private const val UpdateLastInitializationTimestamp = "updateLastInitializationTimestamp "
        private const val SHOULD_FOREGROUND = "startforegroundservice "
        private const val CALLER_ID_OPTIONS = "callerIdOptions "
        private const val IS_FIRST_OPEN = "isFirstOpen "
        private const val CONTACT_SYNCED = "contactSynced "
        private const val USER_ID = "userId "
        private const val INIT_X = "initialX"
        private const val INIT_Y = "initialY"
        private const val CALLER_ID_TYPE = "CALLER_ID_TYPE"
        private const val MOBILE_NUMBER = "mobile_number"
        private const val SET_DFAULT_DONE = "SET_DFAULT_DONE"
        private const val GEMINI_API_KEY = "gemini_api_key"
        private const val GOOGLE_ACCOUNT_EMAIL = "google_account_email"
        private const val PERSONAL_NUMBERS = "personal_numbers"
    }

}