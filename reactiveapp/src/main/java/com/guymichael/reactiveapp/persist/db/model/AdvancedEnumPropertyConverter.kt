package com.guymichael.reactiveapp.persist.db.model

import io.objectbox.converter.PropertyConverter

/**
 * For best practice - have a local-db-unique-key, to prevent changes in enum names or ordinals breaking persist
 */
abstract class AdvancedEnumPropertyConverter<E>: PropertyConverter<E, Int>
    where E: Enum<E>, E: AdvancedEnumPropertyConverterEnum {

    final override fun convertToEntityProperty(databaseValue: Int?): E? {
        return if (databaseValue == null) {
            convertToEntityProperty_defaultIfDbValueNull()
        } else {
            values().find { it.getUniqueLocalDbKey() == databaseValue }
                ?: convertToEntityProperty_defaultIfDbValueNotFound() //fallback
        }
    }

    final override fun convertToDatabaseValue(entityProperty: E?): Int? {
        return entityProperty?.getUniqueLocalDbKey()
    }

    /** Default impl. returns null */
    open fun convertToEntityProperty_defaultIfDbValueNull(): E? = null

    /** Value exists in DB, but not found amongst current enum values (meaning, they've changed).
     *  Consider adding and returning an 'UNKNOWN' type.
     *  Default Impl. returns null */
    open fun convertToEntityProperty_defaultIfDbValueNotFound(): E? = null

    /*** @return all enum values */
    abstract fun values(): List<E>
}

interface AdvancedEnumPropertyConverterEnum {
    fun getUniqueLocalDbKey(): Int
}