package com.ruchitech.quicklinkcaller.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.EventHandler
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


abstract class SharedViewModel : ViewModel(), EventHandler {
    //var resource = MutableStateFlow(Resource.initial<Any>())
    private val _circularLoadingIndicator = MutableStateFlow<Boolean>(false)
    val circularLoadingIndicator: StateFlow<Boolean> = _circularLoadingIndicator.asStateFlow()


    init {
        EventEmitter.subscribe(this)
    }

    infix fun EventEmitter.postEvent(event: Event) {
        postEvent(event)
    }

    fun showLoading(){
        _circularLoadingIndicator.value = true
    }

    fun hideLoading(){
        _circularLoadingIndicator.value = false
    }
    override fun handleInternalEvent(event: Event) = Unit

    override fun onCleared() {
        EventEmitter.unsubscribe(this)
        super.onCleared()
    }
}