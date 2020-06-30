package com.guymichael.reactiveapp.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object AndroidUtils {

    @JvmStatic
    fun toast(context: Context, text: CharSequence, longToast: Boolean = false) {
        Toast.makeText(context, text, if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            .show()
    }

    @JvmStatic
    fun toast(context: Context, @StringRes text: Int, longToast: Boolean = false) {
        Toast.makeText(context, text, if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            .show()
    }
}