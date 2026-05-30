package com.ruchitech.quicklinkcaller.retrofit.repository.resource

import com.ruchitech.quicklinkcaller.retrofit.remote.ApiErrorResponse
import com.ruchitech.quicklinkcaller.retrofit.remote.ApiResponse
import com.ruchitech.quicklinkcaller.retrofit.remote.ApiSuccessResponse
import com.ruchitech.quicklinkcaller.retrofit.remote.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


inline fun <REMOTE> networkOnlyResource(
    crossinline fetchFromRemote: () -> Flow<ApiResponse<REMOTE>>,
    crossinline shouldFetchFromRemote: () -> Boolean = { true },
    crossinline processRemoteResponse: (response: ApiSuccessResponse<REMOTE>) -> Unit = { },
    crossinline onFetchFailed: (errorMessage: String) -> Unit = { _: String -> }
) = flow<Resource<REMOTE>> {
    if (shouldFetchFromRemote()) {
        emit(Resource.loading(null))
        fetchFromRemote().collect { apiResponse ->
            when (apiResponse) {
                is ApiSuccessResponse -> {
                    processRemoteResponse(apiResponse)
                    emit(Resource.success(apiResponse.body))
                }

                is ApiErrorResponse -> {
                    onFetchFailed(apiResponse.errorMessage)
                    emit(Resource.error(apiResponse.errorMessage, null))
                }

                else -> {}
            }
        }
    }
}.flowOn(Dispatchers.IO)