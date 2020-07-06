package com.guymichael.reactiveapp.network.model

import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.promise.Optional
import com.guymichael.reactiveapp.network.ApiManager
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import retrofit2.Call
import kotlin.reflect.KClass

/**
 * Wraps a retrofit API [Call] with [APromise] which can be executed and listened to
 * at any desired time.
 */
object ApiRequest {

    /** @param cancelCallOnDispose if the executed promise is canceled (disposed), the `call` will also
     * be [cancelled][Call.cancel]
     *
     * @param errorResponseClass pass to try to parse to this class on [errors][ApiError]
     * or [Any] to skip
     */
    inline fun <reified T : Any> of(call: Call<T>, apiClientName: String
            , errorResponseClass: KClass<*>? = null
            , cancelCallOnDispose: Boolean = true
        ) : APromise<T> {

        var hadError = false

        return APromise<T>(Single.create {
            if( !it.isDisposed) {
                call.enqueue(RetrofitApiCallback.of(it, T::class, apiClientName, errorResponseClass))
            }
        })

        //on promise cancel, cancel the Call
        .catch { hadError = true }
        .finally { resolved ->
            if (cancelCallOnDispose && !resolved && !hadError) {
                Logger.d(ApiRequest::class, "API ${call::class.simpleName} is cancelling " +
                    "due to promise cancel/dispose (" +
                    "API ${(if (call.isExecuted) "already" else "is not")} executed)")

                call.cancel()
            }
        }
    }

    inline fun <S : Any, reified T : Any> of(
            service: KClass<S>
            , apiClientName: String
            , errorResponseType: KClass<*>? = null
            , crossinline callSupplier: (S) -> Call<T>
        ): APromise<T> {

        return of(callSupplier(ApiManager.getOrCreate(service, apiClientName)), apiClientName, errorResponseType)
    }

    /** @param cancelCallOnDispose if the executed promise is canceled (disposed), the `call` will also
     * be [cancelled][Call.cancel] */
    inline fun <reified T : Any, reified E : Any> ofOptional(call: Call<T>, apiClientName: String
            , cancelCallOnDispose: Boolean = true
            , errorResponseType: KClass<E>? = null
        ): APromise<Optional<T>> {

        var hadError = false

        return APromise<Optional<T>>(Single.create {
            if( !it.isDisposed) {
                call.enqueue(RetrofitApiCallback.ofOptional(it, T::class, apiClientName, errorResponseType))
            }
        })

        //on promise cancel, cancel the Call
        .catch { hadError = true }
        .finally { resolved ->
            if (cancelCallOnDispose && !resolved && !hadError) {
                Logger.d(ApiRequest::class, "API ${call::class.simpleName} is cancelling " +
                        "due to promise cancel/dispose (" +
                        "API ${(if (call.isExecuted) "already" else "is not")} executed)")

                call.cancel()
            }
        }
    }

    fun <T> ofEmitter(call: (SingleEmitter<T>) -> Unit) : APromise<T> {
        return APromise(Single.create {
            if( !it.isDisposed) {
                call(it)
            }
        })
    }
}