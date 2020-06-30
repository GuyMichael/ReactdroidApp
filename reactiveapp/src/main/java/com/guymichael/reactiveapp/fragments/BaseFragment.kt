package com.guymichael.reactiveapp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent

/**
 * In charge of all the boilerplate to wrap a reactdroid [AComponent] ('pageComponent)
 * with this fragment, along with some more capabilities like actual user visibility callback.
 */
abstract class BaseFragment<P: OwnProps, C : AComponent<PAGE_PROPS, *, *>, PAGE_PROPS: OwnProps>
    : ComponentFragment<P>() {

    private lateinit var pageComponent: C



    protected abstract fun createPageComponent(layout: View): C
    protected abstract fun mapFragmentPropsToPageProps(props: P): PAGE_PROPS







    final override fun onBindViews(fragmentView: View) {
        this.pageComponent = createPageComponent(fragmentView)
    }

    final override fun onHardwareBackPressed(): Boolean {
        return pageComponent.onHardwareBackPressed()
    }

    override fun render() {
        Logger.e(this.javaClass, "render")
        pageComponent.onRender(mapFragmentPropsToPageProps(this.props))
    }



    //make final
    final override fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        return super.inflateLayout(inflater, container)
    }
    final override fun onBindViewListeners() {}





    override fun componentWillMount() {
        Logger.e(this.javaClass, "componentWillMount")
    }

    override fun componentDidMount() {
        Logger.e(this.javaClass, "componentDidMount")
    }

    override fun componentDidUpdate(prevProps: P, prevState: EmptyOwnState, snapshot: Any?) {
        Logger.e(this.javaClass, "componentDidUpdate")
    }

    override fun componentWillUnmount() {
        Logger.e(this.javaClass, "componentWillUnmount")
    }
}