package com.ruchitech.quicklinkcaller.retrofit.remote

import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import com.ruchitech.quicklinkcaller.retrofit.model.DataByUser
import com.ruchitech.quicklinkcaller.retrofit.model.TestModel
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.AddSecondaryContactResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContactResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchCallLogs
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchCallLogsResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchedContactResult
import com.ruchitech.quicklinkcaller.ui.screens.home.data.SyncCallLogs
import com.ruchitech.quicklinkcaller.ui.screens.home.data.SyncCallLogsResult
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtp
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtpResult
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtp
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtpResult
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppService {
    companion object {
        private const val SEND_OTP = "otp/sendOtp"
        //private const val VERIFY_OTP = "otp/verifyOtp"
        private const val VERIFY_OTP = "otp/verify_token"
        private const val ADD_SECONDARY_CONTACT = "secondaryContacts/addSecondaryContact"
        private const val UPDATE_SECONDARY_CONTACT = "secondaryContacts/updateSecondaryContact"
        private const val DELETE_SECONDARY_CONTACT = "secondaryContacts/deleteSecondaryContact"
        private const val FETCH_SECONDARY_CONTACT = "secondaryContacts/fetchAllSecondaryContacts"
        private const val SYNC_CALL_LOGS = "logs/SyncLogs"
        private const val SYNC_UPDATE_CALL_LOGS = "logs/updateLogs"
        private const val FETCH_CALL_LOGS_BY_DATE = "logs/getAllLogs"
        private const val DELETE_USER = "secondaryContacts/deleteUser"
        private const val SYNC_APP_LOGS = "appLogs"

    }

    @GET(DELETE_USER)
    fun deleteUser(@Query("user_mobile") user_mobile:String): Flow<ApiResponse<BaseResponse>>

    @POST(SEND_OTP)
    fun sendOtp(@Body sendOtp: SendOtp): Flow<ApiResponse<SendOtpResult>>

    @POST(VERIFY_OTP)
    fun verifyOtp(@Body verifyOtp: VerifyOtp): Flow<ApiResponse<VerifyOtpResult>>

    @POST(ADD_SECONDARY_CONTACT)
    fun addSecondaryContact(@Body contacts: List<Contact>): Flow<ApiResponse<AddSecondaryContactResult>>

    @POST(UPDATE_SECONDARY_CONTACT)
    fun updateSecondaryContact(@Body contacts: List<Contact>): Flow<ApiResponse<AddSecondaryContactResult>>
    @POST(DELETE_SECONDARY_CONTACT)
    fun deleteSecondaryContact(@Body contacts: List<DeleteSecondaryContact>): Flow<ApiResponse<DeleteSecondaryContactResult>>

    @POST(FETCH_SECONDARY_CONTACT)
    fun fetchContact(@Body hashMap: DataByUser): Flow<ApiResponse<TestModel>>

    @POST(SYNC_CALL_LOGS)
    fun syncCallLogs(@Body hashMap: List<CallLogDetails>): Flow<ApiResponse<SyncCallLogsResult>>

    @POST(SYNC_UPDATE_CALL_LOGS)
    fun syncUpdateCallLogs(@Body hashMap: List<CallLogDetails>): Flow<ApiResponse<SyncCallLogsResult>>

    @POST(FETCH_CALL_LOGS_BY_DATE)
    fun fetchCallLogs(@Body hashMap: FetchCallLogs): Flow<ApiResponse<FetchCallLogsResult>>

    @POST(SYNC_APP_LOGS)
    fun syncAppLogs(@Body body: AppLogRequest): Flow<ApiResponse<com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse>>

}


