package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.helper.generateUniqueContactUUID
import com.ruchitech.quicklinkcaller.room.Converters
import com.ruchitech.quicklinkcaller.room.DateOrLongConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "contacts")
data class  Contact(
    @PrimaryKey(autoGenerate = false) val contact_uuid: String = generateUniqueContactUUID(
        Math.random().toString()
    ),
    var contact_title: String,
    var contact_mobile: String,
    val email: String?="",
    val address: String?="",
    var created_at: String?,
    var user_uuid: String? = "",
    var isSynced: Boolean = false,
){
    class CreatedAt {
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
}