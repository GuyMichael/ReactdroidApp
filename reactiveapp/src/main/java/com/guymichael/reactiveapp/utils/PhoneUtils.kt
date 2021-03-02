package com.guymichael.reactiveapp.utils

import android.content.Context
import android.telephony.TelephonyManager

object PhoneUtils {

    /**
     * Gets from SIM or from Network if no SIM country found
     */
    @JvmStatic
    fun getCountryCodeIso(appContext: Context): String? {
        return (appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager?)
            ?.let { tm ->
                tm.simCountryIso?.takeIf { it.isNotBlank() }
                ?: tm.networkCountryIso
            }
    }
}