package com.guymichael.reactiveapp.utils

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

object JsonUtils {

    @Volatile private lateinit var moshi: Moshi

    @JvmStatic
    fun init(moshi: Moshi) {
        this.moshi = moshi
    }

    @JvmStatic
    @Throws(IOException::class, JsonDataException::class)
    fun <T : Any> fromJson(json: String, type: Class<T>): T? {
        return moshi.adapter(type).fromJson(json)
    }

    @Throws(IOException::class, JsonDataException::class)
    inline fun <reified T : Any> fromJson(json: String): T? {
        return fromJson(json, T::class.java)
    }

    @JvmStatic
    @Throws(IOException::class, JsonDataException::class)
    fun <T : Any> fromJsonList(json: String, itemType: Class<T>): List<T>? {
        return moshi.adapter<List<T>>(Types.newParameterizedType(List::class.java, itemType)).fromJson(json)
    }

    @JvmStatic
    @Throws(IOException::class, JsonDataException::class)
    inline fun <reified T : Any> fromJsonList(json: String): List<T>? {
        return fromJsonList(json, T::class.java)
    }

    @JvmStatic
    @Throws(AssertionError::class)
    fun <T : Any> toJson(model: T): String? {
        return moshi.adapter(model.javaClass).toJson(model)
    }

    @JvmStatic
    @Throws(AssertionError::class)
    fun <T : List<*>> toJson(model: T): String? {
        return moshi.adapter(List::class.java).toJson(model)
    }

    @JvmStatic
    @Throws(AssertionError::class)
    fun <T : Set<*>> toJson(model: T): String? {
        return moshi.adapter(Set::class.java).toJson(model)
    }

    @JvmStatic
    fun <T : Any> fromJsonOrNull(json: String, type: Class<T>): T? {
        return try {
            fromJson(json, type)
        } catch (e: IOException) {
            null
        } catch (e: JsonDataException) {
            null
        }
    }

    inline fun <reified T : Any> fromJsonOrNull(json: String): T? {
        return fromJsonOrNull(json, T::class.java)
    }

    @JvmStatic
    fun <T : Any> toJsonOrNUll(model: T): String? {
        return try {
            toJson(model)
        } catch (e: AssertionError) {
            null
        }
    }
}