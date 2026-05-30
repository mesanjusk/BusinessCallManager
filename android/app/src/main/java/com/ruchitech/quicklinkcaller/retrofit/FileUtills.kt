package com.ruchitech.quicklinkcaller.retrofit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.AnyRes
import java.io.File


fun getExtension(uri: String): String {
    val dot = uri.lastIndexOf(".")
    return if (dot >= 0) uri.substring(dot) else ""
}

fun getMimeType(file: File): String {
    val extension = getExtension(file.name)
    val mineType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(
            extension.substring(1)
        )
    return mineType ?: "application/octet-stream"
}

fun Context.getUriToResource(
    @AnyRes resourceId: Int
): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + resources.getResourcePackageName(resourceId)
                + '/' + resources.getResourceTypeName(resourceId)
                + '/' + resources.getResourceEntryName(resourceId)
    )
}