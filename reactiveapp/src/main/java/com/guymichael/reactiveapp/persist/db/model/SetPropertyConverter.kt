package com.guymichael.reactiveapp.persist.db.model

import com.guymichael.reactiveapp.utils.JsonUtils
import io.objectbox.converter.PropertyConverter

open class SetPropertyConverter<T>: PropertyConverter<Set<T>, String> {
    final override fun convertToEntityProperty(databaseValue: String?): Set<T>? {
        return if (databaseValue == null) {
            convertToEntityProperty_defaultIfDbValueNull()
        } else {
            JsonUtils.fromJson(databaseValue)
        }
    }

    final override fun convertToDatabaseValue(entityProperty: Set<T>?): String? {
        return entityProperty?.let { JsonUtils.toJson(it) }
    }

    /** Default impl. returns null */
    open fun convertToEntityProperty_defaultIfDbValueNull(): Set<T>? = null
}