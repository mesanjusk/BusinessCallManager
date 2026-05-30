package com.ruchitech.quicklinkcaller.ui.screens.otp.data

import com.google.gson.annotations.SerializedName

data class VerifyOtp(
    @SerializedName("phone")
    val mobile: String,
    val otp: String,
    val token: String = "",
)
