package com.ruchitech.quicklinkcaller.retrofit.remote


import com.google.gson.Gson
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
                                    body.message.ifEmpty { body.errorMessage }.ifEmpty { errorHandler(code = response.code()) },
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

                else -> {
                    // Try to parse the error body for a human-readable message
                    val errorMsg = try {
                        val errorJson = response.errorBody()?.string()
                        if (!errorJson.isNullOrEmpty()) {
                            val base = Gson().fromJson(errorJson, BaseResponse::class.java)
                            base?.message?.ifEmpty { null } ?: errorHandler(code = response.code())
                        } else errorHandler(code = response.code())
                    } catch (e: Exception) {
                        errorHandler(code = response.code())
                    }
                    ApiErrorResponse(errorMsg, response.code())
                }
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