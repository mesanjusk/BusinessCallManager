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
        private const val CREATE_LEAD = "leads/createLead"
        private const val UPDATE_LEAD = "leads/updateLead"
        private const val FETCH_LEADS = "leads/fetchLeads"
        private const val DELETE_LEAD = "leads/deleteLead"
        private const val CREATE_BUSINESS = "business/create"
        private const val JOIN_BUSINESS = "business/join"
        private const val GET_TEAM = "business/getTeam"
        private const val GET_MY_BUSINESS = "business/getMyBusiness"
        private const val CREATE_TASK = "tasks/createTask"
        private const val UPDATE_TASK = "tasks/updateTask"
        private const val FETCH_MY_TASKS = "tasks/fetchMyTasks"
        private const val FETCH_TEAM_TASKS = "tasks/fetchTeamTasks"
        private const val REPORTS_SUMMARY = "reports/summary"
        private const val SUBSCRIPTION_STATUS = "subscription/status"
        private const val SUBSCRIPTION_UPGRADE = "subscription/upgrade"
        private const val SEND_FOLLOWUP = "leads/sendFollowup"

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

    @POST(CREATE_LEAD)
    fun createLead(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(UPDATE_LEAD)
    fun updateLead(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(FETCH_LEADS)
    fun fetchLeads(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(DELETE_LEAD)
    fun deleteLead(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(CREATE_BUSINESS)
    fun createBusiness(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(JOIN_BUSINESS)
    fun joinBusiness(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(GET_TEAM)
    fun getTeam(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(GET_MY_BUSINESS)
    fun getMyBusiness(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(CREATE_TASK)
    fun createTask(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(UPDATE_TASK)
    fun updateTask(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(FETCH_MY_TASKS)
    fun fetchMyTasks(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(FETCH_TEAM_TASKS)
    fun fetchTeamTasks(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(REPORTS_SUMMARY)
    fun getReportsSummary(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @GET(SUBSCRIPTION_STATUS)
    fun getSubscriptionStatus(@Query("user_uuid") userUuid: String): Flow<ApiResponse<Map<String, Any>>>

    @POST(SUBSCRIPTION_UPGRADE)
    fun upgradeSubscription(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

    @POST(SEND_FOLLOWUP)
    fun sendFollowup(@Body body: HashMap<String, Any>): Flow<ApiResponse<Map<String, Any>>>

}


