package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    // Insert multiple contacts at once
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg contacts: Contact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>)


    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts WHERE contact_mobile = :phoneNumber OR contact_mobile = :phoneNumberWithCountryCode LIMIT 1")
    suspend fun getContactByPhoneNumber(
        phoneNumber: String,
        phoneNumberWithCountryCode: String
    ): Contact?

    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val phoneNumberWithCountryCode =
            "+91$normalizedPhoneNumber" // Assuming the country code is always +91
        return getContactByPhoneNumber(normalizedPhoneNumber, phoneNumberWithCountryCode)
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove non-numeric characters
        return if (phoneNumber.startsWith("0")) {
            // Remove leading '0' and add country code
            phoneNumber.substring(1)
        } else if (phoneNumber.startsWith("+91")) {
            phoneNumber.substring(3)
        } else {
            phoneNumber
        }
    }

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<Contact>>

    // Updated query with pagination and sorting
    @Query("SELECT * FROM contacts ORDER BY contact_title ASC LIMIT :pageSize OFFSET :offset")
    fun getContactsPagedSortedByName(pageSize: Int, offset: Int): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY created_at  DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getContactsPagedSortedByNameTest(pageSize: Int, offset: Int): MutableList<Contact>

    @Query("SELECT * FROM contacts WHERE contact_uuid = :contactId LIMIT 1")
    suspend fun getContactById(contactId: String): Contact?
    @Query("SELECT * FROM contacts WHERE contact_mobile = :number LIMIT 1")
    suspend fun getContactByMobile(number: String): Contact?

    @Query("SELECT * FROM contacts WHERE LOWER(contact_title) LIKE LOWER('%' || :searchQuery || '%') OR contact_mobile LIKE '%' || :searchQuery || '%'")
    suspend fun searchContactsByNameOrNumberSortedByName(searchQuery: String): List<Contact>

    // Retrieve contacts in batch with a specific size and offset
    @Query("SELECT * FROM contacts WHERE isSynced = 0 LIMIT :batchSize OFFSET :offset")
    suspend fun getContactsInBatch(offset: Int, batchSize: Int): List<Contact>

    @Query("UPDATE contacts SET isSynced = :isSynced WHERE contact_uuid IN (:contactIds)")
    suspend fun updateIsSynced(contactIds: List<String>, isSynced: Boolean)


    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedContact(deletedContact: DeleteSecondaryContact)

    @Query("SELECT * FROM deleted_contacts LIMIT :batchSize OFFSET :offset")
    fun getAllDeletedContacts(offset: Int, batchSize: Int): List<DeleteSecondaryContact>

    @Query("DELETE FROM deleted_contacts WHERE contact_uuid IN (:uuids)")
    suspend fun deleteDeletedContactsByIds(uuids: List<String>)
}
