package com.guymichael.reactiveapp.utils.model

import android.os.Handler
import android.os.Looper
import java.util.*

/**
 * A trigger to notify when all `dependencyKeys` exist or not (state change).
 * Existence is `true` when all `dependencyKeys` has corresponding values
 * (nullable - even when values are `null` !!)
 */
open class DependencyTrigger(vararg dependencyKeys: String
        , private val callback: (fulfilledState: Boolean, DependencyTrigger) -> Unit
    ) {

    private val mDependencyKeys = setOf(*dependencyKeys)
    private val mValues = HashMap<String, Any?>()
    private val mNotifyHandler: Handler = Handler(Looper.getMainLooper())
    private val mDependencyCheckRunnable = DependencyCheckRunnable()
    private var wasFulfilled: Boolean = false
    private var canNotify = true//default


    fun put(key: String, value: Any?): DependencyTrigger {
        //don't let same value trigger dependency check
        if (this.mValues[key] == value) {
            return this
        }

        //add property
        this.mValues[key] = value

        //check if dependencies are met (on end of execution chain, in case more 'put' calls are chained)
        mNotifyHandler.removeCallbacks(mDependencyCheckRunnable)
        mNotifyHandler.post(mDependencyCheckRunnable)

        return this
    }

    fun remove(key: String): DependencyTrigger {
        //don't let same value trigger dependency check
        if( !this.mValues.containsKey(key)) {
            return this
        }

        //remove property
        this.mValues.remove(key)

        //check if dependencies are met (on end of execution chain, in case more 'put'/'remove' calls are chained)
        mNotifyHandler.removeCallbacks(mDependencyCheckRunnable)
        mNotifyHandler.post(mDependencyCheckRunnable)

        return this
    }

    /**
     * Set whether to notify on fulfillment state changes. Default is [canNotify]
     * @param notify
     */
    fun setNotifyEnabled(notify: Boolean): DependencyTrigger {
        this.canNotify = notify
        return this
    }

    /**
     * Force rechecking, normally if event was set to not notify on fulfilment.
     * If dependecies were met, this will notify the callback
     * @see .setNotifyOnFulfilment
     */
    fun checkAndNotify() {
        this.mDependencyCheckRunnable.checkDependencies(true)
    }

    /** Triggers this event 'now', regardless of missing keys */
    fun forceTrigger() {
        callback(wasFulfilled, this)
    }

    private inner class DependencyCheckRunnable : Runnable {
        override fun run() {
            checkDependencies(canNotify)
        }

        /** @return new fulfilled state */
        fun checkDependencies(notify: Boolean): Boolean {
            for (key in mDependencyKeys) {
                if( !mValues.containsKey(key)) {//allows nulls
                    //not fulfilled
                    if (wasFulfilled) {
                        //state changed

                        wasFulfilled = false

                        if (notify) {
                            callback(false, this@DependencyTrigger)
                        }
                    }

                    return false
                }
            }

            //fulfilled
            if( !wasFulfilled) {
                //state changed

                wasFulfilled = true

                if (notify) {
                    callback(true, this@DependencyTrigger)
                }
            }

            return true
        }
    }
}