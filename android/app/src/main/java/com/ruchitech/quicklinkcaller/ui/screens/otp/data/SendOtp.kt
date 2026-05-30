package com.ruchitech.quicklinkcaller.ui.screens.otp.data

import com.google.gson.annotations.SerializedName

data class SendOtp(
    @SerializedName("phone")
    val mobile: String,
    val message: String = "",
    val type: String = "",
)
