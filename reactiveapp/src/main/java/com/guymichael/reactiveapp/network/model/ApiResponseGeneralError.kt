package com.guymichael.reactiveapp.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponseGeneralError(
    val timestamp: Long? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)