package com.ruchitech.quicklinkcaller.contactutills

class CountryCodes : ArrayList<CountryCodes.CountryCodesItem>(){
    data class CountryCodesItem(
        val code: String, // AF
        val dial_code: String, // +93
        val name: String // Afghanistan
    )
}