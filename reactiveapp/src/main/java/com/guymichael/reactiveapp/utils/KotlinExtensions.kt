package com.guymichael.reactiveapp.utils

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.promise.Promise
import com.guymichael.promise.letIf
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.getIfAlive
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.core.model.AHOC
import com.guymichael.reactdroid.core.model.WithAComponentDataManager
import com.guymichael.reactdroid.core.withAutoCancel
import com.guymichael.reactdroid.core.withGlobalErrorHandling
import com.guymichael.reactdroidflux.model.AConnectedComponentDataManager
import com.guymichael.reactiveapp.activities.BaseActivity
import java.lang.ref.WeakReference

fun ComponentActivity<*>.setStatusBarColor(@ColorRes colorRes: Int, withDarkBlend: Boolean = false) {
    ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, colorRes), withDarkBlend)
}

fun ViewGroup.findChild(predicate: (child: View) -> Boolean): View? {
    return ViewUtils.findChild(this, predicate)
}



/** Works with a context of [BaseActivity] */
fun View.showBlockUiProgress() {
    context?.also {
        Utils.getActivity(it, BaseActivity::class.java)?.showBlockUiProgress()
    }
}

/** Works with a context of [BaseActivity] */
fun AComponent<*, *, *>.showBlockUiProgress() {
    mView.showBlockUiProgress()
}

/** Works with a context of [BaseActivity] */
fun View.dismissProgress() {
    context?.also {
        Utils.getActivity(it, BaseActivity::class.java)?.dismissProgress()
    }
}

/** Works with a context of [BaseActivity] */
fun AComponent<*, *, *>.dismissProgress() {
    mView.dismissProgress()
}

/** Works with a context of [BaseActivity] */
fun <T> APromise<T>.withBlockUiProgress(view: View): APromise<T> {
    val viewRef = WeakReference(view)

    return doOnExecution { viewRef.get()?.showBlockUiProgress() }
        .finally { viewRef.get()?.dismissProgress() }
}

/** Works with a context of [BaseActivity] */
fun <T> APromise<T>.withBlockUiProgress(component: AComponent<*, *, *>): APromise<T> {
    val componentRef = WeakReference(component)

    return doOnExecution { componentRef.get()?.showBlockUiProgress() }
        .finally { componentRef.get()?.dismissProgress() }
}

fun <T> APromise<T>.withBlockUiProgress(context: BaseActivity<*, *, *>): APromise<T> {
    val contextRef = WeakReference(context)

    return doOnExecution { contextRef.get()?.showBlockUiProgress() }
        .finally { contextRef.get()?.dismissProgress() }
}

/**
 * Adds component-related logic to the loader promise:
 *
 * [APromise.withBlockUiProgress], [APromise.withGlobalErrorHandling], [APromise.withAutoCancel]
 *
 * @param shouldReloadData if null, default (props compare) is applied
 * @param showProgressOnMountLoads works when `component` is in a [BaseActivity] context
 * @param showProgressPropsChangeLoads works when `component` is in a [BaseActivity] context
 * @param autoHandleErrors see [APromise.withGlobalErrorHandling]
 * @param autoCancel see [APromise.withAutoCancel]
 */
fun <P : OwnProps, V : View> withSimpleDataManager(
    component: AComponent<P, *, V>
    , loader: (P) -> APromise<*>
    , shouldLoadDataOnMount: (P) -> Boolean = { true }
    , shouldReloadData: ((prevProps: P, nextProps: P) -> Boolean)? = null
    , showProgressOnMountLoads: Boolean = true
    , showProgressPropsChangeLoads: Boolean = false
    , autoHandleErrors: Boolean = true
    , autoCancel: Boolean = false
) : AHOC<P, *, V, *, EmptyOwnState> {

    val componentRef = WeakReference(component)

    return object : AConnectedComponentDataManager<P, P>() {
        override fun mapPropsToDataProps(ownProps: P): P = ownProps
        override fun onComponentDidMount_isDataAlreadyLoaded(dataProps: P): Boolean {
            return !shouldLoadDataOnMount(dataProps)
        }

        override fun loadAndCacheData(nextDataProps: P, isFromPageViewOrContextChange: Boolean): Promise<*> {
            val comp = componentRef.getIfAlive(true)

            return loader(nextDataProps)
                //withBlockUiProgress
                .letIf({ (isFromPageViewOrContextChange && showProgressOnMountLoads)
                        || (!isFromPageViewOrContextChange && showProgressPropsChangeLoads) }) { p ->

                    comp?.let { p.withBlockUiProgress(it) } ?: p
                }
                //withGlobalErrorHandling
                .letIf({ autoHandleErrors }) { p ->
                    comp?.let { p.withGlobalErrorHandling(it) } ?: p
                }
                //withAutoCancel
                .letIf({ autoCancel }) { p ->
                    comp?.let { p.withAutoCancel(it) } ?: p
                }
        }

        override fun shouldReloadData(prevDataProps: P, nextDataProps: P): Boolean {
            //this is the main idea of this class - we override the default prop equality
            //with a simple original-props shouldLoadData delegate
            return shouldReloadData?.invoke(prevDataProps, nextDataProps)
                ?: super.shouldReloadData(prevDataProps, nextDataProps)
        }

    }.let { WithAComponentDataManager(component, it) }
}