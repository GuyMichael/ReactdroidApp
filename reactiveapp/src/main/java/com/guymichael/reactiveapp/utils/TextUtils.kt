package com.guymichael.reactiveapp.utils

object TextUtils {

    /**
     * Splits text to array using `regex` and takes the `takeIndex` element
     * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
     */
    @JvmStatic
    fun splitAndTake(text: CharSequence?, regex: String, takeIndex: Int = 0
            , ignoreCase: Boolean = false): CharSequence? {

        return text?.takeIf { it.isNotBlank() }?.let {
            try {
                it.split(regex, limit = takeIndex.coerceAtLeast(0) + 1, ignoreCase = ignoreCase)
                    .toTypedArray()[takeIndex]
            } catch (e: IndexOutOfBoundsException) {null}
        }
    }
}