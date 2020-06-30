package com.guymichael.reactiveapp.persist.db.model

import io.objectbox.converter.PropertyConverter

abstract class SimpleEnumPropertyConverter<E: Enum<E>>: PropertyConverter<E, String> {
    final override fun convertToEntityProperty(databaseValue: String?): E? {
        return if (databaseValue == null) {
            convertToEntityProperty_defaultIfDbValueNull()
        } else {
            values().find { it.name == databaseValue } ?: convertToEntityProperty_defaultIfDbValueNotFound()
        }
    }

    final override fun convertToDatabaseValue(entityProperty: E?): String? {
        return entityProperty?.name
    }

    /** Default impl. returns null */
    open fun convertToEntityProperty_defaultIfDbValueNull(): E? = null

    /** Value exists in DB, but not found amongst current enum values (meaning, they've changed).
     *  Consider adding and returning an 'UNKNOWN' type.
     *  Default Impl. returns null. */
    open fun convertToEntityProperty_defaultIfDbValueNotFound(): E? = null

    /*** @return all enum values */
    abstract fun values(): Array<E>
}

/*  usage example:

    //note: it's considered a good practice to not use enum names (nor ordinal), in case they'll change.
    //      But in this scenario the names are final enough to consider a (future) different name as a different type
    class SensorTypeConverter: SimpleEnumPropertyConverter<SomeEnum>() {
        override fun values() = SomeEnum.values()
        override fun convertToEntityProperty_defaultIfDbValueNull() = convertToEntityProperty_defaultIfDbValueNotFound()//we don't want nullable
        override fun convertToEntityProperty_defaultIfDbValueNotFound() = SomeEnum.UNKNOWN//fallback, we don't want nullable
    }

 */