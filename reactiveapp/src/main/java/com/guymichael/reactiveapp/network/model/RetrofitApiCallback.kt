package com.guymichael.reactiveapp.network.model

import com.guymichael.promise.Optional
import io.reactivex.rxjava3.core.SingleEmitter
import kotlin.reflect.KClass

/** A callback for retrofit API Calls, suitable for Promise creation (through the [emitter] member) */
class RetrofitApiCallback<T : Any> private constructor(
        emitter: SingleEmitter<T>
        , apiClientName: String
        , responseClass: KClass<T>
        , errorResponseClass: KClass<*>? = null
    ): BaseRetrofitApiCallback<T, T>(emitter, apiClientName, responseClass, errorResponseClass) {

    override fun mapResponseToEmission(response: T) = response

    companion object {
        fun <R : Any> of(emitter: SingleEmitter<R>
                , responseType: KClass<R>
                , apiClientName: String
                , errorResponseType: KClass<*>? = null
            )
                : RetrofitApiCallback<R> {
            return RetrofitApiCallback(emitter, apiClientName, responseType, errorResponseType)
        }

        fun <R : Any> ofOptional(emitter: SingleEmitter<Optional<R>>
                , responseType: KClass<R>
                , apiClientName: String
                , errorResponseType: KClass<*>? = null
            ): OptionalRetrofitApiCallback<R> {

            return OptionalRetrofitApiCallback(emitter, apiClientName, responseType, errorResponseType)
        }
    }
}






class OptionalRetrofitApiCallback<T : Any> internal constructor(
        emitter: SingleEmitter<Optional<T>>
        , apiClientName: String
        , responseClass: KClass<T>
        , errorResponseType: KClass<*>? = null
    ): BaseRetrofitApiCallback<T, Optional<T>>(emitter, apiClientName, responseClass, errorResponseType) {

    override fun onSuccess(response: T?) {
        if (response == null) {
            //note: we assume that there's no (Java) use for Optional<Unit>
            emitter.onSuccess(Optional.empty())
        } else {
            emitter.onSuccess(mapResponseToEmission(response))
        }
    }

    override fun mapResponseToEmission(response: T) = Optional(response)
}