package com.guymichael.reactiveapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.guymichael.reactiveapp.getString

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

    @SuppressLint("ResourceType")
    @JvmStatic
    fun getColorAsString(@ColorRes colorRes: Int): String {
        return getString(colorRes).replace("#ff", "#")
    }

    @JvmStatic
    fun openEmailTo(context: Context, mail: String): Boolean {
        val emailIntent = Intent(Intent.ACTION_VIEW, Uri.fromParts("mailto", mail, null))

        return try {
            startExternalActivity_notPure(context, emailIntent)
            true
        } catch (e: ActivityNotFoundException) {
            //broken link
            false
        }
    }
}



/* Privates */

/** NOT pure - may affect `intent` */
private fun startExternalActivity_notPure(context: Context, intent: Intent) {
    val callingActivity: Activity? = com.guymichael.reactdroid.core.Utils.getActivity(context)

    if (callingActivity == null) {
        //note: FLAG_ACTIVITY_NEW_TASK *must* be present or the app will crash,
        // as we call startActivity from a non-Activity context:
        // see: https://developer.android.com/about/versions/pie/android-9.0-changes-all#fant-required

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//THINK not pure!!
        context.startActivity(intent)
    } else {
        callingActivity.startActivity(intent)
    }
}