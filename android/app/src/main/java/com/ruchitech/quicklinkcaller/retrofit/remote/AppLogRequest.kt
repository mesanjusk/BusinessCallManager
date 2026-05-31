package com.ruchitech.quicklinkcaller.retrofit.remote

import com.google.gson.annotations.SerializedName

data class AppLogRequest(
    @SerializedName("user_uuid") val user_uuid: String,
    @SerializedName("logs") val logs: String,
)
