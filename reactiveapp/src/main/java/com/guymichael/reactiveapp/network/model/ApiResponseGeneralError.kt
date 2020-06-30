package com.guymichael.reactiveapp.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponseGeneralError(val message: String? = null)