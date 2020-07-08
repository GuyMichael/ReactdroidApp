package com.guymichael.reactiveapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.dialog.CProgressDialog
import com.guymichael.reactdroid.extensions.components.dialog.ProgressDialogProps
import com.guymichael.reactdroid.extensions.components.progressbar.SimpleProgressProps
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import com.guymichael.reactdroid.extensions.navigation.withStoreNavigation
import com.guymichael.reactiveapp.fragments.BaseFragment

/**
 * Contains boilerplate to wrap a reactdroid [AComponent] ('pageComponent)
 * with this activity, along with some more capabilities like showing a progress dialog
 */
abstract class BaseActivity<P : OwnProps, C : AComponent<PAGE_PROPS, *, *>, PAGE_PROPS: OwnProps>
    : ComponentActivity<P>(), LifecycleOwner {

    /* activity */
    protected var finishOnActionBarBack = true

    /* drawer */
    private var appBarConfiguration: AppBarConfiguration? = null
    @IdRes private var navHostFragmentId: Int? = null //see onSupportNavigateUp
    private var drawerController: NavController? = null
        set(value) {
            if (value == null && field != null) {
                throw IllegalArgumentException("drawerController can't be reset once initialized")
            }
            field = value
        }

    /* loader */
    private val blockUiProgress: CProgressDialog by lazy {
        CProgressDialog(Utils.getActivityView(this)!! //THINK null view?
            , onDismiss = {
                blockUiProgress.onRender(
                    ProgressDialogProps(false, progressProps = SimpleProgressProps())
                )
            })
    }

    /* component */
    protected abstract val clientPage: ClientPageIntf?
    private lateinit var pageComponent: C



    /* abstract */
    @MenuRes
    protected abstract fun getMenuRes(): Int?
    protected abstract fun createPageComponent(activityView: ViewGroup): C
    protected abstract fun mapActivityPropsToPageProps(props: P): PAGE_PROPS
    /** Any activity-related preparations, such as [withDrawer] or [withToolbar] */
    protected abstract fun onPrepareActivityFrame()








    /* API */

    protected fun withToolbar(@IdRes id: Int, asActionBar: Boolean = true): Toolbar {
        return findViewById<Toolbar>(id).also {

            if (asActionBar) {
                setSupportActionBar(it)
            }
        }
    }

    protected fun withFab(@IdRes id: Int, onClick: ((FloatingActionButton) -> Unit)? = null)
            : FloatingActionButton {

        return findViewById<FloatingActionButton>(id).also { fab ->

            onClick?.let { l ->
                fab.setOnClickListener {
                    l.invoke(it as FloatingActionButton)
                }
            }
        }
    }

    /**
     * To set the drawer's menu items/actions, set the navId's ([NavigationView]) app:menu property
     * with the relevant menu reference
     *
     * @param drawerId reference to the (top-activity) layout (of type [DrawerLayout]) which holds
     * @param navId reference to the drawer ([NavigationView]) layout
     */
    protected fun withDrawer(@IdRes drawerId: Int, @IdRes navId: Int, @IdRes fragmentId: Int
            , @IdRes vararg menuItems: Int) {

        val drawerLayout: DrawerLayout = findViewById(drawerId)
        val navView: NavigationView = findViewById(navId)
        drawerController = findNavController(fragmentId)

        navHostFragmentId = fragmentId //see onSupportNavigateUp

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration(menuItems.toSet(), drawerLayout).also {
            appBarConfiguration = it
            setupActionBarWithNavController(drawerController!!, it)
        }

        navView.setupWithNavController(drawerController!!)
    }

    fun hasDrawer(): Boolean {
        return navHostFragmentId != null
    }

    /** Set the progress shown/hidden along with all other props.
     * For easy showing, use [showBlockUiProgress].
     * For easy dismissal, use [dismissProgress] */
    fun setProgress(props: ProgressDialogProps) {
        blockUiProgress.onRender(props)
    }

    /** dismiss with [dismissProgress] */
    fun showBlockUiProgress() {
        blockUiProgress.onRender(ProgressDialogProps(true
            , progressProps = SimpleProgressProps()
        ))
    }

    fun dismissProgress() {
        blockUiProgress.onRender(ProgressDialogProps(false
            , progressProps = SimpleProgressProps()
        ))
    }








    /* Lifecycle - Reactdroid */

    /** Call super to keep default logic such as Store navigation */
    final override fun componentWillMount() {
        clientPage?.let(::withStoreNavigation)
    }

    final override fun onBindViews(activityView: ViewGroup) {
        onPrepareActivityFrame()
        this.pageComponent = createPageComponent(activityView)
    }

    final override fun onBindViewListeners() {}

    final override fun onHardwareBackPressed(): Boolean {
        return pageComponent.onHardwareBackPressed()
            || super.onHardwareBackPressed()
    }

    final override fun render() {
        pageComponent.onRender(mapActivityPropsToPageProps(this.props))
    }





    /* Lifecycle - Android */

    final override fun onBackPressed() {
        if( !onFragmentsBackPressed() && !onHardwareBackPressed()) {
            super.onBackPressed()
        } //else -> consumed
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        //allow normal 'back' (no drawer) to work as up (using manifest to set parent activity)
        supportActionBar?.also {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    final override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu)
            && (getMenuRes()?.takeIf { it != 0 }?.let {
                menuInflater.inflate(it, menu)
                true

            } ?: false) //no menu
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == android.R.id.home && !hasDrawer() -> {
                //back pressed
                setResult(RESULT_CANCELED)
                if (!onToolbarBackPressed()) {
                    if (this.finishOnActionBarBack) {
                        onBackPressed()
                        true
                    } else {
                        //"back  - navigate up"
                        try {
                            NavUtils.navigateUpFromSameTask(this)
                            //TODO:
//                            overridePendingTransition(0, R.anim.activity_close_exit)
                            true
                        } catch (e: IllegalArgumentException) {
                            //metadata not configured in manifest to have a parent
                            super.onOptionsItemSelected(item)
                            //THINK fallback to onBackPressed + true
                        }
                    }
                } else {
                    true
                }
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * @return true to consume event and cancel all handling.
     */
    protected open fun onToolbarBackPressed(): Boolean {
        return false
    }

    final override fun onSupportNavigateUp(): Boolean {
        return  navHostFragmentId?.let { fragId ->//we cache the id and not the controller in case view has been reset
                appBarConfiguration?.let { config ->

            findNavController(fragId).navigateUp(config) || super.onSupportNavigateUp()
        }}

        ?: super.onSupportNavigateUp() //no drawer fragment
    }





    /* Privates */

    //pass on back presses to (component) fragments
    private fun onFragmentsBackPressed(): Boolean {
        //click 'back' on the foreground fragment
        for (frag in supportFragmentManager.fragments) {

            //back press on a NavHostFragment child, if a BaseFragment
            if (frag is NavHostFragment) {
                for (navFragChild in frag.childFragmentManager.fragments) {
                    if (navFragChild is BaseFragment<*, *, *> && navFragChild.isMounted()) {
                        return navFragChild.onHardwareBackPressed()
                    }
                }
            }

            //back press the fragment if a BaseFragment
            else if (frag is BaseFragment<*, *, *> && frag.isMounted()) {
                return frag.onHardwareBackPressed()
            }
        }

        return false //no foreground fragment found
    }

    /**
     * @param resId an {@link NavDestination#getAction(int) action} id or a destination id to
     *              navigate to
     * @throws NoSuchFieldException if there's no drawer for this activity
     * @throws IllegalArgumentException if there's no match found for `resId`
     */
    @Throws(NoSuchFieldException::class, IllegalArgumentException::class)
    fun openDrawerDeepLink(@IdRes resId: Int, props: OwnProps) {
        return drawerController?.navigate(resId, Bundle().also {
            it.putSerializable(ComponentFragment.ARGS_KEY_PROPS, props)
        })

        //throws IllegalArgumentException if no match which rejects the promise
        ?: throw NoSuchFieldException("no drawer for this activity to open deep link on")
    }
}