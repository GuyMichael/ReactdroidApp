package com.guymichael.reactiveapp.reactdroid.components.image

import androidx.annotation.DrawableRes
import com.guymichael.reactdroid.extensions.components.image.BaseImageProps
import com.squareup.picasso.Transformation

data class ImageProps(
    override val remoteUrl: String?
    , @DrawableRes override val localOrOnError: Int?
    , @DrawableRes override val remotePlaceholder: Int? = localOrOnError
    , val transformations: List<Transformation>? = null
) : BaseImageProps(remoteUrl, localOrOnError, remotePlaceholder) {

    companion object {
        fun listOf(list: List<String>
                , @DrawableRes placeholder: Int
                , @DrawableRes onError: Int? = placeholder
                , transformations: List<Transformation>? = null
            ): List<ImageProps> {

            return list.map { url -> ImageProps(
                url
                , onError
                , placeholder
                , transformations
            )}
        }

        /** @param list of non-zero, local drawable resources */
        fun listOf(list: List<Int>): List<ImageProps> {
            return list.map { res -> ImageProps(
                null
                , null
                , res
            )}
        }

        /** @param pairs should have at least one non-null/non-zero member.
         * contains the remote url and/or a local res (if has remote, local res will be the 'onError' res)
         * @param placeholder for until the remote image has been loaded
         */
        fun listOf(vararg pairs: Pair<String?, Int?>, @DrawableRes placeholder: Int?): List<ImageProps> {
            return pairs.map { urlAndRes -> ImageProps(
                urlAndRes.first
                , urlAndRes.second
                , placeholder
            )}
        }

        /** @param remoteErrorPlaceholder should have at least one non-null member between first and second.
         * each triple contains:
         * 1. first - the remote url
         * 2. second - onError res
         * 3. third - placeholder for until the remote image has been loaded
         */
        fun listOf(vararg remoteErrorPlaceholder: Triple<String?, Int?, Int?>): List<ImageProps> {
            return remoteErrorPlaceholder.map { urlErrorPlaceholder -> ImageProps(
                urlErrorPlaceholder.first
                , urlErrorPlaceholder.second
                , urlErrorPlaceholder.third
            )
        }}
    }
}