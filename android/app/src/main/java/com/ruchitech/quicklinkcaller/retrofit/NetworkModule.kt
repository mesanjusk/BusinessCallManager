package com.ruchitech.quicklinkcaller.retrofit


import android.util.Log
import com.ruchitech.quicklinkcaller.retrofit.remote.AppService
import com.ruchitech.quicklinkcaller.retrofit.remote.FlowCallAdapter
import com.ruchitech.quicklinkcaller.retrofit.remote.ProgressRequestBody
import com.ruchitech.quicklinkcaller.retrofit.remote.ProgressResponseBody
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ruchitech.quicklinkcaller.BuildConfig.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): AppService =
        retrofit.create(AppService::class.java)


    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(FlowCallAdapter.create())
            .build()


    @Singleton
    @Provides
    fun provideClient(
        logger: HttpLoggingInterceptor,
        interceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder().apply {
            addInterceptor(logger)
            addInterceptor(interceptor)
            callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()

    @Singleton
    @Provides
    fun provideLogger():  HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            // Log only the URL
            if (message.startsWith("--> POST") || message.startsWith("--> GET")) {
                //Log.d("API_REQUEST", message.replace("http","kttp"))
            } else if (message.startsWith("<--") && message.contains("HTTP/")) {
                //Log.d("API_RESPONSE", message)
            } else {
                try {
                    val json = JSONObject(message)
                //    Log.d("API_LOG", json.toString(4))
                } catch (e: JSONException) {
                    // Not a JSON format, log as is
                   // Log.d("API_LOG", message)
                }
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun provideInterceptor(): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder().apply {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")

                /* addHeader(DEVICE_TYPE_HEAD, DEVICE_TYPE_HEAD_VALUE)
                 addHeader(APP_VERSION_HEAD, APP_VERSION_HEAD_VALUE)*/
/*                if (!MpCngApplication1.instance.pref.authToken.isNullOrEmpty()) {
                    //addHeader(AUTHORIZATION_HEAD, MpCngApplication.instance.pref.authToken!!)
                    Log.d("authToken", "${MpCngApplication1.instance.pref.authToken!!}")
                }*/


                if (request.header("Content-Encoding") == "upload-process")
                    method(request.method, request.body?.let { body -> ProgressRequestBody(body) })
            }

            val response: Response = chain.proceed(builder.build())

            if (response.request.header("Content-Encoding") == "download-process") {
                return@Interceptor response.newBuilder()
                    .body(response.body?.let { ProgressResponseBody(it) })
                    .build()
            }

            return@Interceptor response
        }

    private fun bodyToString(body: ResponseBody?): String {
        return try {
            val buffer = Buffer()
            body?.source()?.readAll(buffer)
            buffer.readString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Unable to read response body"
        }
    }


    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    companion object {
        const val CALL_TIMEOUT: Long = 60
        const val CONNECT_TIMEOUT: Long = 60
        const val READ_TIMEOUT: Long = 60
        const val WRITE_TIMEOUT: Long = 60

        private const val DEVICE_TYPE_HEAD = "deviceType"
        private const val DEVICE_TYPE_HEAD_VALUE = "android"
        private const val APP_VERSION_HEAD = "appVersion"
        private const val APP_VERSION_HEAD_VALUE = "BuildConfig.VERSION_NAME"
        private const val AUTHORIZATION_HEAD = "Authorization"
    }
}