/*
 * *******************************************************************************
 *  ** Copyright (C), 2014-2021, OnePlus Mobile Comm Corp., Ltd
 *  ** All rights reserved.
 *  *******************************************************************************
 */

package io.pascucci.repos

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class AsyncResult<out R> {

    data class Success<out T>(val data: T) : AsyncResult<T>()
    data class Error(val exception: Exception) : AsyncResult<Nothing>()
    object Loading : AsyncResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * `true` if [AsyncResult] is of type [AsyncResult.Success] & holds non-null [AsyncResult.Success.data].
 */
@Suppress("unused")
val AsyncResult<*>.succeeded
    get() = this is AsyncResult.Success && data != null
