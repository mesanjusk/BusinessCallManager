package com.ruchitech.quicklinkcaller.retrofit.remote

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type


class CallAdapter(
    private val type: Type
) : CallAdapter<Type, Call<ApiResponse<Type>>> {
    override fun responseType() = type
    override fun adapt(call: Call<Type>) = ResultAdapter(call)

    class ResultAdapter<T>(proxy: Call<T>) : CallDelegate<T, ApiResponse<T>>(proxy) {
        override fun enqueueImplementation(callback: Callback<ApiResponse<T>>) =
            proxy.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(
                        this@ResultAdapter,
                        Response.success(ApiResponse.create(response))
                    )
                }


                override fun onFailure(call: Call<T>, t: Throwable) = callback.onResponse(
                    this@ResultAdapter, Response.success(ApiResponse.create(t))
                )

            })

        override fun cloneImplementation() = ResultAdapter(proxy.clone())

    }
}