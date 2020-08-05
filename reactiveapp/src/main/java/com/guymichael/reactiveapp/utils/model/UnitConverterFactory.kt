package com.guymichael.reactiveapp.utils.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/** https://github.com/square/retrofit/issues/2329 */
class UnitConverterFactory {

    @ToJson
    fun toJson(value: Unit): String {
        return ""
    }

    @FromJson
    fun fromJson(value: String) = Unit
}