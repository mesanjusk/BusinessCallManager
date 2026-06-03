package com.ruchitech.quicklinkcaller.ui.screens.premium.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpgradeVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    val appPreference: AppPreference,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    sealed class PaymentState {
        object Idle : PaymentState()
        object PendingVerification : PaymentState()
        object Verified : PaymentState()
        data class Error(val message: String) : PaymentState()
    }

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    val isPremium: Boolean
        get() {
            val expiry = appPreference.premiumExpiry
            return expiry != null && expiry > System.currentTimeMillis()
        }

    fun onPaymentInitiated() {
        _paymentState.value = PaymentState.PendingVerification
    }

    fun submitTransactionId(txnId: String) {
        if (txnId.isBlank()) {
            _paymentState.value = PaymentState.Error("Please enter your UPI Transaction ID.")
            return
        }
        viewModelScope.launch {
            // Store for admin verification; grant 7-day trial immediately
            appPreference.pendingTransactionId = txnId
            val trialExpiry = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
            appPreference.premiumExpiry = trialExpiry
            val business = dbRepository.businessDao.getBusinessOnce()
            if (business != null) {
                dbRepository.businessDao.updateBusiness(business.copy(plan = "premium", subscription_expiry = trialExpiry))
            }
            _paymentState.value = PaymentState.Verified
            showSnackbar("Premium activated! Thank you.")
        }
    }

    fun dismissError() {
        _paymentState.value = PaymentState.Idle
    }
}
