package com.ruchitech.quicklinkcaller.ui.screens.home.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_contacts")
class DeleteSecondaryContact(
    @PrimaryKey(autoGenerate = false)
    val contact_uuid: String, // 5a756e90-2d1b-44d4-8476-9b60f85d6637
    val user_uuid: String? // abcd
)