package com.guymichael.reactiveapp.network.model

import com.guymichael.promise.Optional
import io.reactivex.rxjava3.core.SingleEmitter
import kotlin.reflect.KClass

/** A callback for retrofit API Calls, suitable for Promise creation (through the [emitter] member) */
class RetrofitApiCallback<T : Any> private constructor(
        emitter: SingleEmitter<T>
        , apiClientName: String
        , responseClass: KClass<T>
    ): BaseRetrofitApiCallback<T, T>(emitter, apiClientName, responseClass) {

    override fun mapResponseToEmission(response: T) = response

    companion object {
        fun <R : Any> of(emitter: SingleEmitter<R>, responseType: KClass<R>, apiClientName: String)
                : RetrofitApiCallback<R> {
            return RetrofitApiCallback(emitter, apiClientName, responseType)
        }

        fun <R : Any> ofOptional(emitter: SingleEmitter<Optional<R>>, responseType: KClass<R>
                , apiClientName: String
            ): OptionalRetrofitApiCallback<R> {

            return OptionalRetrofitApiCallback(emitter, apiClientName, responseType)
        }
    }
}






class OptionalRetrofitApiCallback<T : Any> internal constructor(
        emitter: SingleEmitter<Optional<T>>
        , apiClientName: String
        , responseClass: KClass<T>
    ): BaseRetrofitApiCallback<T, Optional<T>>(emitter, apiClientName, responseClass) {

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