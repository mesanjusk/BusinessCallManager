package com.ruchitech.quicklinkcaller.ui.screens.home.data

import com.google.gson.annotations.SerializedName
import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import com.ruchitech.quicklinkcaller.room.data.Contact

data class FetchedContactResult(
    @SerializedName("contacts")
    val contacts: List<Contact>
):BaseResponse()