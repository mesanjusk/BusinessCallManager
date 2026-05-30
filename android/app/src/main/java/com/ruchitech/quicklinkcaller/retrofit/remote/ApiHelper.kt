package com.ruchitech.quicklinkcaller.retrofit.remote


import com.ruchitech.quicklinkcaller.retrofit.getMimeType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.ForwardingSink
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.File
import java.io.IOException



lateinit var progressListener: ProgressListener

class ProgressRequestBody(
    private val requestBody: RequestBody
) : RequestBody() {

    private lateinit var bufferedSink: BufferedSink

    override fun contentType() = requestBody.contentType()

    override fun contentLength() = try {
        requestBody.contentLength()
    } catch (exception: IOException) {
        exception.printStackTrace()
        -1
    }

    override fun writeTo(sink: BufferedSink) = try {
        if (!::bufferedSink.isInitialized) {
            bufferedSink = countingSink(sink).buffer()
        }
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()

    } catch (exception: IOException) {
        exception.printStackTrace()
        progressListener.onError(exception.localizedMessage ?: "")
        throw exception
    }

    private fun countingSink(sink: BufferedSink) =
        object : ForwardingSink(sink) {
            private var totalBytesRead = 0L
            override fun write(source: Buffer, byteCount: Long) {
                try {
                    super.write(source, byteCount)
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    progressListener.onError(exception.localizedMessage ?: "")
                    throw exception
                }
                totalBytesRead += byteCount
                progressListener.onProgress(
                    totalBytesRead, contentLength(),
                    (100 * totalBytesRead) / contentLength()
                )
            }
        }
}

class ProgressResponseBody(
    private val responseBody: ResponseBody
) : ResponseBody() {

    private lateinit var bufferedSource: BufferedSource

    override fun contentType() = responseBody.contentType()

    override fun contentLength() = responseBody.contentLength()

    override fun source(): BufferedSource {
        if (!::bufferedSource.isInitialized) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource
    }

    private fun source(source: Source): Source =
        object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0L
                progressListener.onProgress(
                    totalBytesRead,
                    contentLength(),
                    (100 * totalBytesRead) / contentLength()
                )
                return bytesRead
            }
        }
}


fun String.createRequestBody(): RequestBody =
    toRequestBody(MULTIPART_FORM_DATA.toMediaTypeOrNull())

fun File?.createMultipartBody(
    param: String
): MultipartBody.Part {

    this?.let {
        return MultipartBody.Part.createFormData(
            param, name,
            this.asRequestBody(MULTIPART_FORM_DATA.toMediaTypeOrNull())
        )
    }
    return MultipartBody.Part.createFormData("", "")
}

fun File.createMultipartWithProgressBody(
    param: String
): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        param, name,
        ProgressRequestBody(asRequestBody(getMimeType(this).toMediaTypeOrNull()))
    )

interface ProgressListener {
    fun onProgress(bytesRead: Long, contentLength: Long, progress: Long)
    fun onError(exception: String)
}


const val MULTIPART_FORM_DATA = "multipart/form-data"