package com.ruchitech.quicklinkcaller.ui.screens.home.data

class AddSecondaryContact : ArrayList<AddSecondaryContact.AddSecondaryContactItem>(){
    data class AddSecondaryContactItem(
        val contact_mobile: String, // 123123123
        val contact_title: String, // abcf
        val created_at: Int, // 123456678
        val user_uuid: String // abcd
    )
}