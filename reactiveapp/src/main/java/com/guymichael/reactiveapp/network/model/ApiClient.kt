package com.guymichael.reactiveapp.network.model

import com.guymichael.reactiveapp.R
import com.guymichael.reactiveapp.getString
import com.guymichael.reactiveapp.network.ApiManager.init
import retrofit2.Retrofit

/**
 *
 * Holds a [Retrofit] client and its cached services (retrofit api interface),
 * along with client-specific configurations
 *
 * @param client the [Retrofit] client
 * @param clientName a **unique** name (best to use `@StringDef`)
 * @param defaultApiErrorMsgSupplier a default, fallback message, to be used/displayed to user
 * in case the (error) response contains no message (e.g. from api or an exception)
 * @param apiResponseCodeSupplier parse the api response code to be used by you if you ever need
 * to do certain actions upon receiving certain api errors. Apart from the non-null mapping, it is not
 * used by this library
 */
open class ApiClient(
    val client: Retrofit,
    val clientName: String,
    val apiResponseCodeSupplier: (Int?) -> Int = { it ?: -1 },
    val defaultApiErrorMsgSupplier: () -> String = { getString(R.string.network_default_unknown_api_error) }
) {

    private val callsCache = HashMap<Class<out Any>, Any>()


    /**
     * @param service a retrofit service interface class
     * @param clientName the [ApiClient] to be used. Name as given in [init]
     * @throws [NullPointerException] if client was not found
     */
    internal fun <T : Any> getOrCreate(service: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return (callsCache[service] as? T?) //THINK sync
            ?: client.create(service).also {

                //cache new service/call
                callsCache[service] = it
            }
    }

    internal fun clearCache() {
        callsCache.clear() //THINK sync
    }
}