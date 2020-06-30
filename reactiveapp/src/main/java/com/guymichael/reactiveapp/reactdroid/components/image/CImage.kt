package com.guymichael.reactiveapp.reactdroid.components.image

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactiveapp.R
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.image.BaseImageComponent
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Transformation

class CImage(
        v: ImageView
        , private val remoteAttributesSetter: ((RequestCreator) -> RequestCreator)?
    ) : BaseImageComponent<ImageProps, EmptyOwnState, ImageView>(v) {

    constructor(v: ImageView): this(v, null)

    override fun createInitialState(props: ImageProps) = EmptyOwnState

    override fun renderRemoteImage(url: String, onErrorRes: Int?, placeholder: Int?) {
        Picasso.get()
            .load(url)
            .apply { placeholder?.let(::placeholder) }
            .let { remoteAttributesSetter?.invoke(it) ?: it }
            .apply { onErrorRes?.let(::error) }
            .into(mView)
    }

    fun render(remoteUrl: String?
            , @DrawableRes placeholder: Int = R.drawable.img_remote_default
            , @DrawableRes onErrorImg: Int = placeholder
            , transformations: List<Transformation>? = null
        ) {

        onRender(ImageProps(remoteUrl, onErrorImg, placeholder, transformations))
    }
}

/*class CAvatar(v: ImageView) : CImage(v) {
    override fun renderRemoteImage(url: String, onErrorRes: Int?, placeholder: Int?) {
        Picasso.get()
            .load(url)
            .transform(CropCircleTransformation())
            .apply { placeholder?.let(::placeholder) }
            .centerCrop()
            .fit()
            .noFade()
            .apply { onErrorRes?.let(::error) }
            .into(mView)
    }
}*/


//THINK as Annotations
fun View.withImage(@IdRes id: Int, remoteAttributesSetter: ((RequestCreator) -> RequestCreator)? = null)
    = CImage(
    findViewById(id),
    remoteAttributesSetter
)

fun AComponent<*, *, *>.withImage(@IdRes id: Int, remoteAttributesSetter: ((RequestCreator) -> RequestCreator)? = null)
    = CImage(
    mView.findViewById(id),
    remoteAttributesSetter
)

//fun View.withAvatar(@IdRes id: Int) = CAvatar(findViewById(id))
//fun AComponent<*, *, *>.withAvatar(@IdRes id: Int) = CAvatar(mView.findViewById(id))