package com.guymichael.reactiveapp.utils

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.guymichael.reactdroid.core.activity.ComponentActivity

fun ComponentActivity<*>.setStatusBarColor(@ColorRes colorRes: Int, withDarkBlend: Boolean = false) {
    ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, colorRes), withDarkBlend)
}

fun ViewGroup.findChild(predicate: (child: View) -> Boolean): View? {
    return ViewUtils.findChild(this, predicate)
}