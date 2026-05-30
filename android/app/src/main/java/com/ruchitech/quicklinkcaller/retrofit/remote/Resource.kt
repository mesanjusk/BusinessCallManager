package com.ruchitech.quicklinkcaller.retrofit.remote

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {

    companion object {
        fun <T> initial(): Resource<T> {
            return Resource(
                Status.INITIAL,
                null,
                null
            )
        }

        fun <T> empty(msg: String?): Resource<T> {
            return Resource(
                Status.EMPTY,
                null,
                msg
            )
        }

        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(msg: String?, data: T? = null): Resource<T> {
            return Resource(
                Status.ERROR,
                data,
                msg
            )
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(
                Status.LOADING,
                data,
                null
            )
        }
    }
}

enum class Status {
    INITIAL,
    EMPTY,
    SUCCESS,
    ERROR,
    LOADING
}