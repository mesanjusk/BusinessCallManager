package com.ruchitech.quicklinkcaller.retrofit

import android.os.SystemClock
import android.util.ArrayMap
import java.util.concurrent.TimeUnit


class RateLimiter<in KEY>(timeOut: Int, timeUnit: TimeUnit) {
    private val timestamps = ArrayMap<KEY, Long>()
    private val timeOut = timeUnit.toMillis(timeOut.toLong())

    @Synchronized
    fun shouldFetch(key: KEY): Boolean {
        val lastFetched = timestamps[key]
        val now = now()
        return when {
            lastFetched == null -> {
                timestamps[key] = now

                true
            }

            now - lastFetched > timeOut -> {
                timestamps[key] = now
                true
            }

            else -> {
                false
            }
        }
    }

    private fun now() = SystemClock.uptimeMillis()

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }
}