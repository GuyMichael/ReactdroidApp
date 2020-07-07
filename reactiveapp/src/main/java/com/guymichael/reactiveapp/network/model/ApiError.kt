package com.guymichael.reactiveapp.network.model

import com.guymichael.reactiveapp.network.ApiManager

/**
 * A model returned by network/api requests on http failures (not 200).
 * @param errorBody if the error response model is of the same type as the success one,
 * the body might be parsed successfully to return a valid response (of that model type).
 * Otherwise, it will be a okhttp3 `ResponseBody`, or null*/
data class ApiError(val errorBody: Any?, val e: Throwable?, val code: Int, val apiClientName: String)
    : Throwable(e?.message?.takeIf { it.isNotBlank() } ?: ApiManager.getDefaultErrorMsg(apiClientName)) {

    override val message: String
        get() = super.message!! //we make sure it's not null in constructor
}