package com.guymichael.reactiveapp

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import com.guymichael.reactdroid.extensions.components.permissions.PermissionsLogic
import com.guymichael.reactdroid.extensions.navigation.NavigationLogic
import com.guymichael.reactiveapp.network.ApiManager
import com.guymichael.reactiveapp.network.model.ApiClient
import com.guymichael.reactiveapp.persist.db.DbLogic
import com.guymichael.reactiveapp.persist.sharedpref.SharedPrefLogic
import com.guymichael.reactiveapp.utils.JsonUtils
import com.squareup.moshi.Moshi
import io.objectbox.BoxStore
import retrofit2.converter.moshi.MoshiConverterFactory

abstract class BaseApplication : MultiDexApplication() {

    companion object {
        internal lateinit var INSTANCE: BaseApplication

        private const val SHARED_PREF_KEY_DENIED_PERMISSIONS = "deniedPermissionsSet"
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        //note: order matters!

        initErrorHandling()
        initLogging()

        //init json parser first. Needed for db, api, etc.
        initDefaultJsonParser(Moshi.Builder()).also {
            JsonUtils.init(it)
        }


        initSharedPref()            //before permissions (requires sharedPref persist)
        initPermissionsLogic()

        //init db, before Store, so that reducer might use initial state from db
        initDb().also {
            DbLogic.init(it)
        }

        //init reactdroid Store. After db for reducer's default state
        initStore()

        initDeepLink()

        initApiClients(MoshiConverterFactory.create()).also {
            ApiManager.init(it)
        }

        NavigationLogic.init(this)
    }






    protected abstract fun initErrorHandling()
    protected abstract fun initLogging()

    protected open fun initDefaultJsonParser(builder: Moshi.Builder): Moshi {
        return builder
            //add custom adapters here
            .build()
    }

    protected open fun initSharedPref() {
        SharedPrefLogic.init(this)
    }

    protected open fun initPermissionsLogic() {
        PermissionsLogic.init(
            { SharedPrefLogic.putStringSet(SHARED_PREF_KEY_DENIED_PERMISSIONS, it) }
            , { SharedPrefLogic.getStringSet(SHARED_PREF_KEY_DENIED_PERMISSIONS) }
        )
    }

    protected abstract fun initDb(): BoxStore

    protected abstract fun initStore()
    protected abstract fun initDeepLink()

    /** Call `NetworkManager.init` */
    protected abstract fun initApiClients(defaultJsonParser: MoshiConverterFactory): List<ApiClient>
}




@ColorInt
fun getColor(@ColorRes color: Int): Int {
    return ContextCompat.getColor(BaseApplication.INSTANCE, color)
}

fun getText(@StringRes resId: Int): CharSequence {
    return BaseApplication.INSTANCE.getText(resId)
}

fun getDimenPx(@DimenRes resId: Int): Int {
    return BaseApplication.INSTANCE.resources.getDimensionPixelSize(resId)
}

fun getString(@StringRes textRes: Int, vararg format: Any): String {
    return BaseApplication.INSTANCE.getString(textRes, *format)
}

fun getAppContext(): Context {
    return BaseApplication.INSTANCE.applicationContext
}