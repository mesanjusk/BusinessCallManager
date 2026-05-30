package com.ruchitech.quicklinkcaller.retrofit.remote


import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import okhttp3.Headers
import retrofit2.Response

sealed class ApiResponse<T> {
    companion object {

        fun <T> create(error: Throwable): ApiErrorResponse<T> =
            ApiErrorResponse(
                errorHandler(error = error),
                0
            )

        fun <T> create(response: Response<T>): ApiResponse<T> =
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    val header = response.headers()
                    when {
                        body == null || response.code() == 204 -> ApiEmptyResponse()
                        else -> when {
                            body is BaseResponse && !body.isSuccess -> {
                                ApiErrorResponse(
                                    body.errorMessage,
                                    response.code()
                                )
                            }
                            else -> {
                                ApiSuccessResponse(
                                    body,
                                    header
                                )
                            }
                        }
                    }
                }

                else -> ApiErrorResponse(
                    errorHandler(code = response.code()),
                    response.code()
                )
            }
    }
}

class ApiEmptyResponse<T> : ApiResponse<T>()

data class ApiSuccessResponse<T>(
    val body: T?,
    val header: Headers
) : ApiResponse<T>()

data class ApiErrorResponse<T>(
    val errorMessage: String,
    val statusCode: Int
) : ApiResponse<T>()