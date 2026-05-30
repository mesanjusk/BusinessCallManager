package com.ruchitech.quicklinkcaller.contactutills

import android.R.attr.resource
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ruchitech.quicklinkcaller.contactutills.parsers.PhoneNumberParser
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.checkPermissions
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.room.data.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class ContactHelper @Inject constructor(
    private val context: Context,
    private val phoneNumberParser: PhoneNumberParser,
) {

    suspend fun getContactDetailsByPhoneNumber(phoneNumber: String): ContactDetails {
        return withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

            // Clean the input phone number
            val cleanedPhoneNumber = cleanPhoneNumber(phoneNumber)

            // Build a list of variations to search for
            val variations = buildPhoneNumberVariations(cleanedPhoneNumber)


            // Initialize contact details with empty values
            var contactDetails = ContactDetails("", "", "", "")

            // Use a higher-order function to handle non-local return
            fun findContactDetails(): Boolean {
                for (variation in variations) {
                    val cursor: Cursor? = contentResolver.query(
                        contactUri,
                        getProjection(),
                        "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                        arrayOf(variation),
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            contactDetails = ContactDetails(
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
                            )
                            Log.e("fdkjghh", "findContactDetails:2 $contactDetails")
                            return true  // Use the labeled return to exit the function
                        }
                    }

                    cursor?.close()
                }
                return false
            }

            // Call the higher-order function
            if (!findContactDetails()) {
                contactDetails = ContactDetails("Unknown", phoneNumber, "", "")
            }
            contactDetails
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): String {
        // Remove non-digit characters and leading "+" if present
        return phoneNumber.replace(Regex("\\D"), "")
    }

    private fun buildPhoneNumberVariations(phoneNumber: String): List<String> {
        val trimmedPhoneNumber =
            if (phoneNumber.length > 10) phoneNumber.substring(phoneNumber.length - 10) else phoneNumber
        return listOf(
            phoneNumber,                   // Original format
            trimmedPhoneNumber,           // trimmedPhoneNumber format
            "+$phoneNumber",                // With leading "+"
            "+91$phoneNumber",              // With country code
            "+91-$phoneNumber",             // With country code and hyphen
            "+91 $phoneNumber",             // With country code and space
            "0$phoneNumber",                // With leading zero
            "+91-0$phoneNumber",             // With country code and leading zero
            "+91 0$phoneNumber"             // With country code, space, and leading zero
            // Add more variations as needed
        )
    }

    private fun getProjection(): Array<String> {
        // Define the columns to retrieve from the contacts database
        return arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
        )
    }

    suspend fun getNameFromPhoneNumber(number: String): String {/*        if (!context.hasPermission(PERMISSION_READ_CONTACTS)) {
                    return number
                }*/

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex =
                        cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    cursor.getString(displayNameIndex)
                } else {
                    "Unknown"
                }
            } ?: number
        } catch (e: Throwable) {
            // Log or report the exception
            "Unknown"
        }
    }

    suspend fun exportContactsToVCard(contacts: List<Contact>, onSuccess: (msg: String) -> Unit) {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val vCardFile = File(downloadDir, "contacts.vcf")
        try {
            val fileOutputStream = FileOutputStream(vCardFile)
            for (contact in contacts) {
                val vCardString = convertContactToVCardString(contact)
                fileOutputStream.write(vCardString.toByteArray())
                fileOutputStream.write("\n".toByteArray()) // Add a newline separator between vCards
            }
            fileOutputStream.close()
            // Notify the user that export is successful
            Log.e("gfdgfdgfdg", "Contacts exported successfully to ${vCardFile.absolutePath}")

            onSuccess("Contacts exported successfully to ${vCardFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            // Notify the user if export fails
            Log.e("gfdgfdgfdg", "Failed to export contacts")

        }
    }

    private suspend fun convertContactToVCardString(contact: Contact): String {
        val vCardBuilder = StringBuilder()
        // Begin vCard
        vCardBuilder.append("BEGIN:VCARD\n")
        vCardBuilder.append("VERSION:3.0\n")
        // Add contact information
        vCardBuilder.append("FN:${contact.contact_title}\n")
        vCardBuilder.append("TEL;TYPE=CELL:${contact.contact_mobile}\n")
        // Add more fields as needed (email, address, etc.)
        // End vCard
        vCardBuilder.append("END:VCARD\n")
        return vCardBuilder.toString()
    }



    suspend fun searchContacts(query: String): SnapshotStateMap<String?, String?> {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null
        )
        val hashMap = SnapshotStateMap<String?, String?>()
        cursor?.use { c ->
            while (c.moveToNext()) {
                val name =
                    c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var number =
                    c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val parsedMap = phoneNumberParser.phoneNumberData(number, "IN")
                val countryCode = parsedMap["countryCode"] as? String // Country code (e.g., "91")
                val phoneNumber =
                    parsedMap["number"] as? String // National number (e.g., "1234567890")
                number = "+$countryCode $phoneNumber"

                if (phoneNumber.isNullOrEmpty()) {
                    number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }

                hashMap[number] = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                Log.d("ContactSearch", "Name: $name, Number: $number")
            }
        }
        return hashMap
    }

}

data class ContactDetails(
    val displayName: String, val phoneNumber: String, val email: String, val address: String,
)
