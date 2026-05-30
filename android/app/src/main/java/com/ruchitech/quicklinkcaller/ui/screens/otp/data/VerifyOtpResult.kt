package com.ruchitech.quicklinkcaller.ui.screens.otp.data

import com.google.gson.annotations.SerializedName
import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse

data class VerifyOtpResult(
    @SerializedName("user_uuid")
    val userId: String?
) : BaseResponse()
