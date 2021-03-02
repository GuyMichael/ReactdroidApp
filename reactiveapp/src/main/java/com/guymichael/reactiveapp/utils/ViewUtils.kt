package com.guymichael.reactiveapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.R
import androidx.core.graphics.ColorUtils
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.model.AComponent

object ViewUtils {


    /** Works for API [Build.VERSION_CODES.LOLLIPOP] and above. */
    @JvmStatic
    @JvmOverloads
    fun setStatusBarColor(activityContext: Context, @ColorInt color: Int
            , withDarkBlend: Boolean = false
        ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.getActivity(activityContext)?.also { activity ->
                val window = activity.window

                // clear FLAG_TRANSLUCENT_STATUS flag:
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                // finally change the color
                window.statusBarColor = if (withDarkBlend)
                    ColorUtils.blendARGB(color, 0, 0.1f)
                    else color
            }
        }
    }

    fun isViewVisibleInParent(parent: View, view: View, fullyVisible: Boolean): Boolean {
        //note: we're double checking lots of parameters. This is because of two things:
        //1. Some method behave differently with each Android version (e.g. return a value considering translation(x/y) or not
        //2. Related to #1, if a view is translated, e.g. in a Fragment, some methods may return values which represent as if they're on screen, but they're not.
        val parentBounds = Rect().also {
            parent.getHitRect(it)
        }
        val childBounds = Rect().also {
            view.getGlobalVisibleRect(it)
        }
        val locationOnWindow = IntArray(2).also {
            view.getLocationInWindow(it)
        }
        val viewX = locationOnWindow[0]
        val viewY = locationOnWindow[1]

        if (childBounds.isEmpty || parentBounds.isEmpty || viewX == 0 && viewY == 0) {
            return false
        }

        return if (fullyVisible) {
            if (parentBounds.contains(childBounds)) {
                childBounds.width() >= view.width
                    && childBounds.height() >= view.height
                    && viewX >= parentBounds.left
                    && viewX + childBounds.width() <= parentBounds.right
                    && viewY >= parentBounds.top
                    && viewY + childBounds.height() <= parentBounds.bottom
            } else false
        } else {
            parentBounds.intersect(childBounds)
                && viewX >= parentBounds.left
                && viewX <= parentBounds.right
                && viewY >= parentBounds.top
                && viewY <= parentBounds.bottom
        }
    }

    @JvmStatic
    fun findChild(parent: ViewGroup, predicate: (child: View) -> Boolean): View? {
        var i = 0
        while (i < parent.childCount) {
            parent.getChildAt(i)?.let {child ->
                //If it's a viewGroup, try to find deeper views which match this predicate (recursive)
                if (ViewGroup::class.java.isInstance(child)) {
                    (child as? ViewGroup)?.let { viewGroupChild ->
                        //try to find a child of this viewGroup child
                        findChild(viewGroupChild, predicate)?.let {
                            return it
                        }
                    }
                }

                //inner child not found, try 'this'
                if (predicate(child)) {
                    return child
                }
            }

            i += 1
        }

        return null
    }

    /**
     * @param context an [Activity] or a [Context] contained inside one
     * @return
     */
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        return Rect().also { rect ->
            Utils.getActivity(context)?.window?.decorView?.also {
                it.getWindowVisibleDisplayFrame(rect)
            }
        }.top
    }

    /**
     * Returns the actual top (Y) of the view, relative to screen.
     * Only relevant if view is measured and visible/attached (or else 0 is returned)
     *
     * @param considerStatusBar If True, consider the status bar in calculations (results in higher value)
     * @return global top (distance) in px
     */
    @JvmStatic
    fun getGlobalTop(view: View, considerStatusBar: Boolean): Int {
        val i = IntArray(2)
        view.getLocationOnScreen(i)

        return try {
            i[1] + if (considerStatusBar) 0 else -getStatusBarHeight(view.context)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the actual top (Y) of the view, relative to screen.
     * Only relevant if view is measured and visible/attached (or else 0 is returned)
     *
     * @param considerStatusBar If True, consider the status bar in calculations (results in higher value)
     * @return global top (distance) in px
     */
    @JvmStatic
    fun getGlobalTop(component: AComponent<*, *, *>, considerStatusBar: Boolean): Int {
        return getGlobalTop(component.mView, considerStatusBar)
    }

    /**
     * @return Actionbar / Toolbar standard height for `context`'s theme or 0 if attribute not found in theme
     */
    @JvmStatic
    fun getActionbarHeight(context: Context, @AttrRes globalAttr: Int = R.attr.actionBarSize): Int {
        // Calculate ActionBar height
        val tv = TypedValue()
        return if (context.theme.resolveAttribute(globalAttr, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        } else 0
    }

    @JvmStatic
    fun asLink(
        text: CharSequence
        , url: String
        , onClick: (View, url: String) -> Unit = { v, urlInner ->
            com.guymichael.reactdroid.extensions.router.Utils.openLink(v.context, urlInner)
        }
    ): Spanned {

        return SpannableString(text).also {
            it.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClick.invoke(widget, url)
                    }
                }

                , 0
                , text.length
                , Spanned.SPAN_INTERMEDIATE
            )
        }
    }
}