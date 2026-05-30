package com.ruchitech.quicklinkcaller.ui.screens.otp.data

data class VerifyOtp(
    val mobile:String,
    val otp:String,
    val token:String="",
)
