package com.ruchitech.quicklinkcaller.ui.screens.home.data

import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse

data class DeleteSecondaryContactResult(
    val failed: List<Any>,
    val succeed: List<String>,
) : BaseResponse()