package com.ruchitech.quicklinkcaller.room

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converters {

    @TypeConverter
    fun listToJsonString(value: List<CallLogDetails>): String =
        Gson().toJson(value)

    @TypeConverter
    fun jsonStringToArrayData(value: String) =
        Gson().fromJson(value, Array<CallLogDetails>::class.java).toList()

    @TypeConverter
    fun listToJsonString2(value: List<Tasks>): String =
        Gson().toJson(value)

    @TypeConverter
    fun jsonStringToArrayData2(value: String?) =
        Gson().fromJson(value?:"[]", Array<Tasks>::class.java).toList()

    @TypeConverter
    fun fromOptionsSet(optionsSet: Set<AllCallerIdOptions>): String {
        return optionsSet.joinToString(separator = ",") { it.name }
    }

    @TypeConverter
    fun toOptionsSet(optionsString: String): Set<AllCallerIdOptions> {
        return optionsString.split(",").map { AllCallerIdOptions.valueOf(it) }.toSet()
    }

    @TypeConverter
    fun fromStringToMap(value: String?): MutableMap<String?, String> {
        val mapType = object : TypeToken<MutableMap<String?, String>>() {}.type
        return Gson().fromJson(value, mapType) ?: mutableMapOf()
    }

    @TypeConverter
    fun fromMapToString(map: MutableMap<String?, String>?): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringToTaskMap(value: String?): MutableMap<String?, List<Tasks?>> {
        if (value == null) {
            return mutableMapOf()
        }
        val gson = Gson()
        val type = object : TypeToken<MutableMap<String?, List<Tasks?>>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTaskMapToString(map: MutableMap<String?, List<Tasks?>>?): String {
        val gson = Gson()
        return gson.toJson(map)
    }

}