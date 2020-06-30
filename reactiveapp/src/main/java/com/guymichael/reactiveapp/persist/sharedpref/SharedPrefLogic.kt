package com.guymichael.reactiveapp.persist.sharedpref

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import com.guymichael.reactiveapp.utils.JsonUtils

object SharedPrefLogic {
    private lateinit var mSharedPref: SharedPreferences

    fun init(app: Application) {
        mSharedPref = app.applicationContext.getSharedPreferences("shared_pref_main", 0)//0 is private
    }

    /**
     * @param persistImmediate if false, commits changes to the in-memory SharedPreferences
     * immediately but starts an asynchronous commit to disk. `true` commits to disk immediately
     * @param consumer to perform the desired `put` action, no further actions required (e.g. no need
     * to [SharedPreferences.Editor.commit])
     */
    @SuppressLint("ApplySharedPref")
    fun put(persistImmediate: Boolean = false, consumer: (SharedPreferences.Editor) -> Unit) {
        mSharedPref.edit().also {
            consumer.invoke(it)

            if (persistImmediate) {
                it.commit()
            } else {
                it.apply()
            }
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putStringSet(key: String, set: Set<String>) {
        mSharedPref.edit().also {
            it.putStringSet(key, set)

            it.apply()
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putBoolean(key: String, value: Boolean) {
        mSharedPref.edit().also {
            it.putBoolean(key, value)

            it.apply()
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putInt(key: String, value: Int) {
        mSharedPref.edit().also {
            it.putInt(key, value)

            it.apply()
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putString(key: String, value: String) {
        mSharedPref.edit().also {
            it.putString(key, value)

            it.apply()
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putLong(key: String, value: Long) {
        mSharedPref.edit().also {
            it.putLong(key, value)

            it.apply()
        }
    }

    /** Puts and [applies][SharedPreferences.Editor.apply].
     * To make use of [SharedPreferences.Editor.commit] instead, use [put] */
    fun putFloat(key: String, value: Float) {
        mSharedPref.edit().also {
            it.putFloat(key, value)

            it.apply()
        }
    }

    /** Puts list as a (json) String.
     * @throws AssertionError if json failed to serialize */
    @Throws(AssertionError::class)
    fun putList(key: String, list: List<*>, persistImmediate: Boolean = false) {
        put(persistImmediate) {
            it.putString(key, JsonUtils.toJson(list))
        }
    }






    fun getStringSet(key: String, defValue: Set<String>? = null): Set<String>? {
        return mSharedPref.getStringSet(key, defValue)
    }






    @SuppressLint("ApplySharedPref")
    fun remove(key: String, persistImmediate: Boolean = false) {
        mSharedPref.edit().also {
            it.remove(key)

            if (persistImmediate) {
                it.commit()
            } else {
                it.apply()
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clearAll(persistImmediate: Boolean = true) {
        mSharedPref.edit().also {
            it.clear()

            if (persistImmediate) {
                it.commit()
            } else {
                it.apply()
            }
        }
    }
}