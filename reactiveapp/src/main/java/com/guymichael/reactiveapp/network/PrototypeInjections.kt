package com.guymichael.reactiveapp.network

import com.guymichael.reactiveapp.network.model.ApiError
import com.guymichael.reactiveapp.network.model.ApiResponseGeneralError

/** @return [ApiResponseGeneralError] or null if `e` is not an instance of [ApiResponseGeneralError] */
fun ApiError.Companion.generalOrNull(e: Throwable): ApiResponseGeneralError? {
    return (e as? ApiError?)?.errorBody as? ApiResponseGeneralError?
}