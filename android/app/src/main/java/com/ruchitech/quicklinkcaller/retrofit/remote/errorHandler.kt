package com.ruchitech.quicklinkcaller.retrofit.remote

import android.util.Log
import com.ruchitech.quicklinkcaller.retrofit.model.BaseResponse
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection

fun errorHandler(
    error: Throwable? = null,
    code: Int? = null,
    baseResponse: BaseResponse? = null
): String {


    val test = when {
        error != null -> {
            when (error) {
                is HttpException -> when (error.code()) {
                    HttpsURLConnection.HTTP_BAD_REQUEST -> BAD_REQUEST
                    HttpsURLConnection.HTTP_UNAUTHORIZED -> UNAUTHORISED
                    HttpsURLConnection.HTTP_FORBIDDEN -> FORBIDDEN
                    HttpsURLConnection.HTTP_NOT_FOUND -> NOT_FOUND
                    HttpsURLConnection.HTTP_UNSUPPORTED_TYPE -> UNSUPPORTED_MEDIA_TYPE
                    HttpsURLConnection.HTTP_INTERNAL_ERROR -> INTERNAL_ERROR
                    else -> error.localizedMessage
                }

                is JsonSyntaxException -> {
                    Log.e("jdfnhusfybsduy", "errorHandler: $error")
                    SOMETHING_WENT_WRONG
                }
                is UnknownHostException -> NO_INTERNET
                else -> error.localizedMessage
            }
        }

        baseResponse != null ->
            when {
                baseResponse.message != "" ->
                    baseResponse.message

                else -> baseResponse.errorMessage
            }

        code != null -> {
            when (code) {
                API_STATUS_CODE_EMPTY_ERROR -> EMPTY_RESPONSE
                API_STATUS_CODE_LOCAL_ERROR -> NO_INTERNET
                API_STATUS_CODE_UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE
                else -> error?.localizedMessage ?: UNKNOWN_ERROR
            }
        }

        else -> UNKNOWN_ERROR
    }
    return test
}

const val API_STATUS_CODE_LOCAL_ERROR = 0
const val API_STATUS_CODE_EMPTY_ERROR = 204
const val API_STATUS_CODE_UNSUPPORTED_MEDIA_TYPE = 415
const val NOT_FOUND_CODE = 404
const val UNAUTHORISED = "Unauthorised User"
const val FORBIDDEN = "Forbidden"
const val NOT_FOUND = "Not Found"
const val INTERNAL_ERROR = "Internal Server Error"
const val BAD_REQUEST = "Bad Request"
const val NO_INTERNET = "No Internet Connection"
const val EMPTY_RESPONSE = "Empty Response"
const val SOMETHING_WENT_WRONG = "Something Went Wrong API is not responding properly!"
const val UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type"
const val UNKNOWN_ERROR = "Unknown Error"