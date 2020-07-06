package com.guymichael.reactiveapp.network.model

import com.guymichael.reactiveapp.network.ApiManager
import com.guymichael.reactiveapp.utils.JsonUtils
import io.reactivex.rxjava3.core.SingleEmitter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.reflect.KClass

/** A callback for retrofit API [Call]s, suitable for Promise creation (through the [emitter] member) */
abstract class BaseRetrofitApiCallback<R : Any, T>(
        protected val emitter: SingleEmitter<T>
        , protected val apiClientName: String
        , protected val responseClass: KClass<R>
        , protected val errorResponseClass: KClass<*>? = null
    ): Callback<R> {


    abstract fun mapResponseToEmission(response: R): T



    /* Overrides */

    override fun onResponse(call: Call<R>, response: Response<R>) {
        if (response.isSuccessful) {
            onSuccess(response.body())
        } else {
            emitter.onError(ApiError(
                parseErrorBody(response.errorBody())
                //if parsing failed, pass over the error body, raw (nullable)
                    ?: response.errorBody()
                , Throwable(response.message())
                , response.code()
                , apiClientName
            ))
        }
    }

    override fun onFailure(call: Call<R>, e: Throwable) {
        emitter.onError(
            ApiError(null
                , e
                , ApiManager.parseResponseCode(null, apiClientName)
                , apiClientName
            )
        )
    }




    /* protected callbacks/APIs */

    protected open fun onSuccess(response: R?) {
        if (response == null) {
            //try to replace null with T, in case it's a kotlin Unit for example
            try {
                @Suppress("UNCHECKED_CAST")
                emitter.onSuccess(Unit as T)
            } catch (e: ClassCastException) {
                emitter.onError(
                    ApiError(null, null
                        , ApiManager.parseResponseCode(null, apiClientName)
                        , apiClientName
                    )
                )
            }
        } else {
            emitter.onSuccess(mapResponseToEmission(response))
        }
    }

    protected open fun parseErrorBody(errorBody: ResponseBody?): Any? {
        return errorBody?.charStream()?.let {  reader ->
            reader.readText()
                .let(::parseErrorBody)
                .also { reader.close() }
        }
    }

    protected open fun parseErrorBody(json: String): Any? {
            //try parsing to error response type ('E')
        return errorResponseClass?.let { JsonUtils.fromJsonOrNull(json, it.java) }

            //or to standard response type ('R')
            ?: JsonUtils.fromJsonOrNull(json, responseClass.java)

            //or to some general error model
            ?: JsonUtils.fromJsonOrNull(json, ApiResponseGeneralError::class.java)
    }
}