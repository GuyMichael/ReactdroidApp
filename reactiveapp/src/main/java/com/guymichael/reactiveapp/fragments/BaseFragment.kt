package com.guymichael.reactiveapp.fragments

import android.view.*
import androidx.annotation.MenuRes
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactiveapp.model.ScreenPage

/**
 * In charge of all the boilerplate to wrap a reactdroid [AComponent] ('pageComponent)
 * with this fragment, along with some more capabilities like actual user visibility callback.
 */
abstract class BaseFragment<P: OwnProps, C : AComponent<PAGE_PROPS, *, *>, PAGE_PROPS: OwnProps>
    : ComponentFragment<P>() {

    private lateinit var pageComponent: C

    private var menu: Menu? = null



    protected abstract fun createPageComponent(layout: View): C
    protected abstract fun mapFragmentPropsToPageProps(props: P): PAGE_PROPS
    @MenuRes
    protected open fun getMenuRes(): Int? = null





    /* Reactdroid */

    final override fun onBindViews(fragmentView: View) {
        this.pageComponent = createPageComponent(fragmentView)
    }

    final override fun onHardwareBackPressed(): Boolean {
        return pageComponent.onHardwareBackPressed()
    }

    protected open fun renderMenu(menu: Menu) {}

    final override fun render() {
        Logger.d(this.javaClass, "render")
        pageComponent.onRender(mapFragmentPropsToPageProps(this.props))
        menu?.also(::renderMenu)
    }



    //make final
    final override fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        return super.inflateLayout(inflater, container)
    }
    final override fun onBindViewListeners() {}


    override fun componentWillMount() {
        Logger.d(this.javaClass, "componentWillMount")
    }

    override fun componentDidMount() {
        Logger.d(this.javaClass, "componentDidMount")
    }

    override fun componentDidUpdate(prevProps: P, prevState: EmptyOwnState, snapshot: Any?) {
        Logger.d(this.javaClass, "componentDidUpdate")
    }

    override fun componentWillUnmount() {
        Logger.d(this.javaClass, "componentWillUnmount")
    }





    /* Android */

    final override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        (getMenuRes()?.takeIf { it != 0 })?.also {
            menuInflater.inflate(it, menu)

            this.menu = menu
        }
    }

    final override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return pageComponent.let { page ->
            if (page is ScreenPage) {
                page.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
            } else {
                super.onOptionsItemSelected(item)
            }
        }
    }
}