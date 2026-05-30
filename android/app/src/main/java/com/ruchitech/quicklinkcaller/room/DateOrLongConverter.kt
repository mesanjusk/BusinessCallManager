package com.ruchitech.quicklinkcaller.room

import androidx.room.TypeConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateOrLongConverter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    @TypeConverter
    fun fromAny(value: Any?): Long? {
        if (value is Long) {
            return value
        } else if (value is String) {
            try {
                return dateFormat.parse(value)?.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return null
    }

    @TypeConverter
    fun toAny(value: Long?): Any? {
        return value
    }
}
