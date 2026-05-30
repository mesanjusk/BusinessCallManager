package com.ruchitech.quicklinkcaller.retrofit.repository


import com.ruchitech.quicklinkcaller.retrofit.RateLimiter
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import com.ruchitech.quicklinkcaller.retrofit.model.DataByUser
import com.ruchitech.quicklinkcaller.retrofit.model.TestModel
import com.ruchitech.quicklinkcaller.retrofit.remote.AppService
import com.ruchitech.quicklinkcaller.retrofit.remote.Resource
import com.ruchitech.quicklinkcaller.retrofit.repository.resource.networkOnlyResource
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.AddSecondaryContactResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContactResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchCallLogs
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchCallLogsResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.SyncCallLogsResult
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtp
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtpResult
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtp
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtpResult
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AccountRepository
@Inject constructor(
    private val appService: AppService,
    private val appPreference: AppPreference
) {
    private val repoListRateLimit = RateLimiter<String>(1, TimeUnit.SECONDS)

    fun sendOtp(sendOtp: SendOtp): Flow<Resource<SendOtpResult>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.sendOtp(sendOtp)
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("sendOtp") })
    }
    fun deleteUser(): Flow<Resource<BaseResponse>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.deleteUser(appPreference.mobileNumber?:"")
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("${Date()}user") })
    }

    fun verifyOtp(verifyOtp: VerifyOtp): Flow<Resource<VerifyOtpResult>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.verifyOtp(verifyOtp)
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("verifyOtp") })
    }

    //923224287594

    fun syncContacts(contact: List<Contact>): Flow<Resource<AddSecondaryContactResult>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.addSecondaryContact(contact)
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncContacts${Date().time}") })
    }

    fun syncUpdatedContacts(contact: List<Contact>): Flow<Resource<AddSecondaryContactResult>> {
        return networkOnlyResource(
            fetchFromRemote = {
                appService.updateSecondaryContact(contact)
            },
            shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncUpdatedContacts${Date().time}") })
    }

    fun syncCallLogs(contact: List<CallLogDetails>): Flow<Resource<SyncCallLogsResult>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.syncCallLogs(contact)
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncCallLogs${Date().time}") })
    }

    fun syncUpdateCallLogs(contact: List<CallLogDetails>): Flow<Resource<SyncCallLogsResult>> {
        return networkOnlyResource(
            fetchFromRemote = {
                appService.syncUpdateCallLogs(contact)
            },
            shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncUpdateCallLogs${Date().time}") })
    }

    fun fetchCallLogs(fetchCallLogs: FetchCallLogs): Flow<Resource<FetchCallLogsResult>> {
        return networkOnlyResource(fetchFromRemote = {
            appService.fetchCallLogs(fetchCallLogs)
        }, shouldFetchFromRemote = { repoListRateLimit.shouldFetch("fetchCallLogs${Date().time}") })
    }

    fun syncDeletedContacts(contacts: List<DeleteSecondaryContact>): Flow<Resource<DeleteSecondaryContactResult>> {
        return networkOnlyResource(
            fetchFromRemote = {
                appService.deleteSecondaryContact(contacts)
            },
            shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncDeletedContacts${Date().time}") })
    }

    fun syncFetchedContacts(): Flow<Resource<TestModel>> {
        return networkOnlyResource(
            fetchFromRemote = {
                val param = DataByUser(user_uuid = appPreference.userId)
                appService.fetchContact(param)
            },
            shouldFetchFromRemote = { repoListRateLimit.shouldFetch("syncFetchedContacts${Date().time}") })
    }

}