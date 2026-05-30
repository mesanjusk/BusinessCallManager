package com.ruchitech.quicklinkcaller.retrofit.model

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName


open class BaseResponse(
    @SerializedName("appVersion")
    @Ignore
    val appVersion: String = "",
    @SerializedName("errorCode")
    @Ignore
    val code: String = "",
    @SerializedName("datetime")
    @Ignore
    val datetime: String = "",
    @SerializedName("code")
    @Ignore
    val errorCode: Int = 0,
    @SerializedName("errorMsg")
    @Ignore
    val errorMessage: String = "",
    @SerializedName("message")
    @Ignore
    val message: String = "",
    @SerializedName("timestamp")
    @Ignore
    val timestamp: Int = 0,
    @SerializedName("success")
    @Ignore
    val isSuccess: Boolean = false,
    @SerializedName("versionId")
    @Ignore
    val versionId: Int? = 0
)