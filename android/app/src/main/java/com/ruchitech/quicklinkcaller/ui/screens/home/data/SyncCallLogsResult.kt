package com.ruchitech.quicklinkcaller.ui.screens.home.data

import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse

data class SyncCallLogsResult(
    val result: List<Result>
):BaseResponse() {
    data class Result(
        val message: String, // Log added successfully
        val number: String, // +919343222060
        val success: Boolean // true
    )
}