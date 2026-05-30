package com.ruchitech.quicklinkcaller.retrofit.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.awaitResponse
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class FlowCallAdapter : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? = when (getRawType(returnType)) {
        Flow::class.java -> {
            val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
            require(observableType is ParameterizedType) { "resource must be parameterized" }
            val rawObservableType = getRawType(observableType)
            require(rawObservableType == ApiResponse::class.java) { "type must be a resource" }
            val bodyType = getParameterUpperBound(0, observableType)
            ResultAdapter(bodyType)
        }

        Call::class.java -> {
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            when (getRawType(callType)) {
                ApiResponse::class.java -> {
                    val resultType = getParameterUpperBound(0, callType as ParameterizedType)
                    CallAdapter(resultType)
                }

                else -> null
            }
        }

        else -> null
    }

    companion object {
        @JvmStatic
        fun create() = FlowCallAdapter()
    }

    class ResultAdapter(
        private val responseType: Type
    ) : CallAdapter<Type, Flow<ApiResponse<Type>>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<Type>): Flow<ApiResponse<Type>> =
            flow {
                val response = call.awaitResponse()
                emit(ApiResponse.create(response))
            }.catch { error ->
                emit(ApiResponse.create(error))
            }
    }
}