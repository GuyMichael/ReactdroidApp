package com.guymichael.reactiveapp.network.model

import com.guymichael.reactiveapp.network.ApiManager
import io.reactivex.rxjava3.exceptions.CompositeException

/** @param errorBody if the error response model is of the same type as the success one,
 * the body might be parsed successfully to return a valid response (of that model type).
 * Otherwise, it will be a okhttp3 `ResponseBody`, or null*/
data class ApiError(val errorBody: Any?, val e: Throwable?, val code: Int, val apiClientName: String)
    : Throwable(e?.message?.takeIf { it.isNotBlank() } ?: ApiManager.getDefaultErrorMsg(apiClientName)) {

    override val message: String
        get() = super.message!! //we make sure it's not null in constructor

    companion object {
        fun parseMany(source: List<Throwable>) : List<ApiError> {
            return source.map { getApiErrors(it) }.reduce { acc, list ->  acc + list}
        }

        @JvmStatic
        fun parseMany(e: Throwable) : List<ApiError> {
            return getApiErrors(e)
        }
    }
}





fun getApiErrors(e: Throwable) : List<ApiError> {
    if (ApiError::class.java.isInstance(e)) {
        return listOfNotNull(e as? ApiError)

    } else if (CompositeException::class.java.isInstance(e)) {
        (e as? CompositeException)?.let {
            return ApiError.parseMany(it.exceptions)
        }
    }

    return emptyList()
}