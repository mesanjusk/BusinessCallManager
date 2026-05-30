package com.ruchitech.quicklinkcaller.ui.screens.home.data

import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails

data class FetchCallLogsResult(
    val logs: List<CallLogDetails>
) : BaseResponse() {
    data class Log(
        val __v: Int, // 0
        val _id: String, // 65bfa026510988d7e89f50ed
        val cachedName: String, // Rahul Ahirwar
        val callNote: String, // test note m
        val callerId: String, // +919343222060
        val colorCode: String, // -72057594037927936
        val date: String, // 1707057533000
        val duration: String, // 20
        val id: String, // 35378
        val number: String, // +919343222060
        val type: String, // OUTGOING
        val user_uuid: String // c2ad17d2-1c40-4e66-b526-0eb2493af264
    )
}