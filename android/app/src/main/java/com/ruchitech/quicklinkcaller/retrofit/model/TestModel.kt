package com.ruchitech.quicklinkcaller.retrofit.model

import com.ruchitech.quicklinkcaller.room.data.Contact

data class TestModel(
    val contacts: List<com.ruchitech.quicklinkcaller.room.data.Contact>
):BaseResponse() {
    data class Contact(
        val __v: Int, // 0
        val _id: String, // 65bcd97eb91ebabc689edda2
        val contact_mobile: String, // 16855658655
        val contact_title: String, // Test
        val contact_uuid: String, // 95aae9d9-a353-4bb5-a786-4d64e81c6e8b
        val created_at: String, // 1970-01-02T10:17:36.678Z
        val user_uuid: String // 4c68ce23-379c-4eec-b03e-e25f7310f56d
    )
}