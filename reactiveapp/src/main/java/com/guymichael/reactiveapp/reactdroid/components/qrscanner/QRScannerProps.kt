package com.guymichael.reactiveapp.reactdroid.components.qrscanner

import android.util.SparseArray
import android.view.SurfaceHolder
import com.google.android.gms.vision.barcode.Barcode
import com.guymichael.kotlinreact.model.OwnProps

/** @param initial_onScanSucceed consumer for barCodes - never empty when invoked */
data class QRScannerProps(
        val initial_onScanSucceed: (barCodes: SparseArray<Barcode>) -> Unit,
        /** This is called immediately after any structural changes (format or size) have been made
         * to the surface. You should at this point update the imagery in the surface.
         * This method is always called at least once (after camera started),
         * and when the component is mounted */
        val initial_onSurfaceChanged: ((holder: SurfaceHolder, format: Int, width: Int, height: Int) -> Unit)? = null,
        val initial_onErrorStartingCamera: ((e: RuntimeException) -> Unit)? = null
    ) : OwnProps() {

    override fun getAllMembers() = emptyList<Any?>()
}