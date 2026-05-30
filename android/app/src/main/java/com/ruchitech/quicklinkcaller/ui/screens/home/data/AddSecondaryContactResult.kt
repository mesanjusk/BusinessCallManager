package com.ruchitech.quicklinkcaller.ui.screens.home.data

import com.google.gson.annotations.SerializedName
import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse

data class AddSecondaryContactResult(
    val exists: List<Any>,
    val failed: List<Any>,
    @SerializedName("result")
    val result: List<Result>
) : BaseResponse() {
    data class Result(
        val _v: Int, // 0
        val contact_mobile: String, // 123123123
        val contact_title: String, // abcf
        val contact_uuid: String, // 5a756e90-2d1b-44d4-8476-9b60f85d6637
        val id: String, // 65bbe0f7608ffe971a0deca1
        val user_uuid: String // abcd
    )
}