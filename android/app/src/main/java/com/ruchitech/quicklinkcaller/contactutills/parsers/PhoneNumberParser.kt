package com.ruchitech.quicklinkcaller.contactutills.parsers

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import javax.inject.Singleton

@Singleton
class PhoneNumberParser {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val defaultRegion = "IN"

    /**
     * Parses and validates a phone number, returning details about the number.
     */
    fun phoneNumberData(
        phoneNumber: String,
        defaultRegion: String
    ): Map<String, Any?> {
        return try {
            val number = phoneUtil.parse(phoneNumber, defaultRegion)
            if (phoneUtil.isValidNumber(number)) {
                val regionCode = phoneUtil.getRegionCodeForNumber(number)
                val numberType = phoneUtil.getNumberType(number).name

                mapOf(
                    "countryCode" to number.countryCode.toString(),
                    "number" to number.nationalNumber.toString(),
                    "regionCode" to regionCode, // Region code (e.g., IN, US)
                    "type" to numberType, // Type of the number (e.g., MOBILE, FIXED_LINE)
                    "isValid" to true // Indicates if the number is valid
                )
            } else {
                invalidNumberResponse(phoneNumber)
            }
        } catch (e: NumberParseException) {
            invalidNumberResponse(phoneNumber)
        }
    }

    /**
     * Formats a phone number into an international or national format.
     */
    fun formatPhoneNumber(
        phoneNumber: String,
        defaultRegion: String = "IN",
    ): String {
        return try {
            val number = phoneUtil.parse(phoneNumber, defaultRegion)
            if (phoneUtil.isValidNumber(number)) {
               // phoneUtil.format(number, format)
                number.nationalNumber.toString().replace(" ","").trim()
            } else phoneNumber
        } catch (e: NumberParseException) {
            phoneNumber
        }
    }

    /**
     * Checks if a phone number is valid for a specific region.
     */
    fun isValidForRegion(phoneNumber: String, region: String): Boolean {
        return try {
            val number = phoneUtil.parse(phoneNumber, region)
            phoneUtil.isValidNumberForRegion(number, region)
        } catch (e: NumberParseException) {
            false
        }
    }

    /**
     * Extracts the country code from a phone number.
     */
    fun extractCountryCode(phoneNumber: String, defaultRegion: String): Int? {
        return try {
            val number = phoneUtil.parse(phoneNumber, defaultRegion)
            if (phoneUtil.isValidNumber(number)) {
                number.countryCode
            } else null
        } catch (e: NumberParseException) {
            null
        }
    }


    /**
     * Checks if a number is possible (e.g., length, structure) but not necessarily valid.
     */
    fun isPossibleNumber(phoneNumber: String, defaultRegion: String): Boolean {
        return try {
            val number = phoneUtil.parse(phoneNumber, defaultRegion)
            phoneUtil.isPossibleNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    }

    /**
     * Helper function to handle invalid numbers.
     */
    private fun invalidNumberResponse(phoneNumber: String): Map<String, Any?> {
        return mapOf(
            "countryCode" to null,
            "number" to phoneNumber, // Return the input number as-is
            "regionCode" to null,
            "type" to null,
            "isValid" to false // Indicates the number is invalid
        )
    }
}
