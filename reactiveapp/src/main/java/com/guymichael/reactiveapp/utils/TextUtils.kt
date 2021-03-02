package com.guymichael.reactiveapp.utils

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned

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

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    @JvmStatic
    fun fromHtml(source: String?): Spanned {
        return when {
            source.isNullOrBlank()
            -> SpannableStringBuilder()

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            -> Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)

            else -> Html.fromHtml(source)
        }
    }
}