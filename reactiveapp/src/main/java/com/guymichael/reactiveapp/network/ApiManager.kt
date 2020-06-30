package com.guymichael.reactiveapp.network

import com.guymichael.reactiveapp.network.model.ApiClient
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

/** A singleton managing the api calls - [ApiClient]s */
object ApiManager {

    private val apiClients = HashMap<String, ApiClient>()

    fun init(clients: Collection<ApiClient>) {
        //add clients
        for (client in clients) {
            //add client and (also) check for duplicates
            apiClients.put(client.clientName, client)?.also { //prevClientWithSameName ->
                throw IllegalArgumentException("${ApiManager::class.simpleName}#init() : " +
                    "found more than one client with the name ${client.clientName}")
            }
        }
    }




    /* API */

    /**
     * @param service a retrofit service interface class
     * @param clientName the [ApiClient] to be used. Name as given in [init]
     * @throws [NullPointerException] if client was not found
     */
    fun <T : Any> getOrCreate(service: Class<T>, clientName: String): T {
        return getClientUnsafe(clientName).getOrCreate(service)
    }

    fun <T : Any> getOrCreate(service: KClass<T>, clientName: String): T {
        return getOrCreate(service.java, clientName)
    }

    fun clearCache() {
        apiClients.forEach { (_, c) -> c.clearCache() } //THINK sync
    }







    /* internal */

    /**
     * @throws [NullPointerException] if client was not found
     */
    internal fun getDefaultErrorMsg(clientName: String)
        = getClientUnsafe(clientName).defaultApiErrorMsgSupplier.invoke()

    /**
     * @throws [NullPointerException] if client was not found
     */
    internal fun parseResponseCode(rawCode: Int?, clientName: String): Int
        = getClientUnsafe(clientName).apiResponseCodeSupplier.invoke(rawCode)

    private fun getClientUnsafe(name: String): ApiClient {
        return apiClients[name]!! //THINK sync
    }
}