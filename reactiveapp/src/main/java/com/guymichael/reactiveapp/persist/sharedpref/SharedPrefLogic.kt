package com.guymichael.reactiveapp.persist.sharedpref

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactiveapp.BuildConfig
import com.guymichael.reactiveapp.utils.JsonUtils

object SharedPrefLogic {
    private lateinit var mSharedPref: SharedPreferences

    fun init(app: Application, name: String = "shared_pref_main", mode: Int = Context.MODE_PRIVATE) {
        mSharedPref = app.applicationContext.getSharedPreferences(name, mode)
    }



    fun getString(key: String, defValue: String? = null): String? {
        return mSharedPref.getString(key, defValue)
    }

    fun getStringSet(key: String, defValues: Set<String?>? = null): Set<String>? {
        return mSharedPref.getStringSet(key, defValues)
    }

    fun getInt(key: String, defValue: Int? = null): Int? {
        return if (mSharedPref.contains(key)) {
            mSharedPref.getInt(key, defValue ?: -1)
        } else {
            defValue
        }
    }

    fun getLong(key: String?, defValue: Long? = null): Long? {
        return if (mSharedPref.contains(key)) {
            mSharedPref.getLong(key, defValue ?: -1L)
        } else {
            defValue
        }
    }

    fun getFloat(key: String?, defValue: Float? = null): Float? {
        return if (mSharedPref.contains(key)) {
            mSharedPref.getFloat(key, defValue ?: -1F)
        } else {
            defValue
        }
    }

    fun getBoolean(key: String?, defValue: Boolean? = null): Boolean? {
        return if (mSharedPref.contains(key)) {
            mSharedPref.getBoolean(key, defValue ?: false)
        } else {
            defValue
        }
    }

    operator fun contains(key: String): Boolean = mSharedPref.contains(key)

    /**
     * Custom `put`. Consider using of the the specific methods, e.g. [putString]
     *
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @param consumer to perform the desired `put` action, no further actions required (e.g. no need
     * to [SharedPreferences.Editor.commit])
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    @SuppressLint("ApplySharedPref")
    fun put(synchronous: Boolean = false
            , consumer: (SharedPreferences.Editor) -> SharedPreferences.Editor): Boolean? {
        return mSharedPref.edit().let(consumer).save(synchronous, "put")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putString(key: String, value: String?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().putString(key, value)
            .save(synchronous, "putString")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putStringSet(key: String, value: Set<String>?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().putStringSet(key, value)
            .save(synchronous, "putStringSet")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putStringSet(key: String, value: Boolean?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().let {
            if (value == null) {
                it.remove(key)
            } else {
                it.putBoolean(key, value)
            }
        }.save(synchronous, "putStringSet")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putLong(key: String, value: Long?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().let {
            if (value == null) {
                it.remove(key)
            } else {
                it.putLong(key, value)
            }
        }.save(synchronous, "putLong")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putInt(key: String, value: Int?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().let {
            if (value == null) {
                it.remove(key)
            } else {
                it.putInt(key, value)
            }
        }.save(synchronous, "putInt")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putFloat(key: String, value: Float?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().let {
            if (value == null) {
                it.remove(key)
            } else {
                it.putFloat(key, value)
            }
        }.save(synchronous, "putFloat")
    }

    /**
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     */
    fun putBoolean(key: String, value: Boolean?, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().let {
            if (value == null) {
                it.remove(key)
            } else {
                it.putBoolean(key, value)
            }
        }.save(synchronous, "putBoolean")
    }

    /**
     * Puts list as a (json) String.
     *
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument:
     * if argument is `true`, returned value is `true` if writing to disk succeeded, false otherwise.
     * If argument is `false`, returned value is always `null`
     *
     * @throws AssertionError if json failed to serialize
     */
    @Throws(AssertionError::class)
    fun putListOrThrow(key: String, value: List<*>, synchronous: Boolean = false): Boolean? {
        return put(synchronous) {
            it.putString(key, JsonUtils.toJson(value))
        }
    }

    /**
     * Puts list as a (json) String.
     *
     * @param synchronous if true, [SharedPreferences.Editor.commit] is used to synchronously
     * commit given `value` to disk, in which case this method's return value (Boolean) is Non-null,
     * and expresses whether the commit to disk succeeded or not.
     *
     * If false, in-memory commit is immediate as well, but commit to disk is asynchronous,
     * in which case this method will return `null` (always), as the actual commit-to-disk response
     * is unknown
     *
     * @return value depends on the `synchronous` argument AND the to-json serialization success:
     * if argument is `true`, returned value is `true` if writing to disk AND to-json serialization
     * succeeded, false otherwise.
     *
     * If argument is `false`, returned value is `null` if to-json serialization succeeded, or `false`
     * if failed
     */
    fun putList(key: String, value: List<*>, synchronous: Boolean = false): Boolean? {
        return try {
            putListOrThrow(key, value, synchronous)
        } catch (e: AssertionError) {
            false
        }
    }

    fun remove(key: String, synchronous: Boolean = false): Boolean? {
        return mSharedPref.edit().remove(key).save(synchronous, "remove")
    }

    /**
     * Clears all [SharedPreferences], synchronously by default(!)
     */
    fun clearAll(synchronous: Boolean = true): Boolean? {
        return mSharedPref.edit().clear().save(synchronous, "clearAll")
    }
}



private fun SharedPreferences.Editor.save(synchronous: Boolean, methodNameForLogging: String): Boolean? {
    return if (synchronous) {
        commit().also {
            if (BuildConfig.DEBUG && Utils.isOnUiThread()) {
                Logger.w(SharedPrefLogic::class, "$methodNameForLogging() was called on main thread" +
                        "with 'synchronous' argument 'true'. Please make sure this is intended" +
                        "or switch to 'synchronous' = 'false'"
                )
            }
        }
    } else {
        apply()
        null
    }
}