package com.guymichael.reactiveapp.reactdroid.components.qrscanner

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.IdRes
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.ownstate.BooleanState
import com.guymichael.kotlinreact.setState
import com.guymichael.reactdroid.core.ViewUtils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.execute
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent
import io.reactivex.rxjava3.schedulers.Schedulers


class CQRScanner(v: SurfaceView) : AComponent<QRScannerProps, BooleanState, SurfaceView>(v) {
    override fun createInitialState(props: QRScannerProps) = BooleanState(false) //whether or not surface is ready

    private val barcodeDetector = BarcodeDetector.Builder(mView.context)
        .setBarcodeFormats(Barcode.ALL_FORMATS)
        .build()

    private val cameraSource = CameraSource.Builder(mView.context, barcodeDetector)
        .setAutoFocusEnabled(true)
        .build()

    private var isCameraActive = false
    private var isHoldingCamera = false

    private val cameraScheduler by lazy { Schedulers.io() }

    //this callback uses our props, so it must be used after componentDidMount or we'll have a race condition
    private val mSurfaceHolderCallback by lazy { object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            //we want to wait for actual user-visibility of the camera before starting,
            // e.g. when inside a list (recycler) or a pager, where views are mounted before shown
            //THINK create a HOC to do that
            ViewUtils.waitForViewOnScreen(mView, false, 0).then {
                setState(true)
            }.execute(this@CQRScanner)
        }
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            setState(false)
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            //we must be mounted at this point
            props.initial_onSurfaceChanged?.invoke(holder, format, width, height)
        }
    }}


    init {
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                //To prevent memory leaks barcode scanner has been stopped
                //THINK
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                detections.detectedItems.takeIf { it.isNotEmpty() }?.also {
                    mView.post { props.initial_onScanSucceed.invoke(it) }
                }
            }
        })
    }

    override fun componentDidMount() {
        mView.holder.removeCallback(mSurfaceHolderCallback) //no-op on first time
        mView.holder.addCallback(mSurfaceHolderCallback)
        //THINK this is a race because if addCallback would've been called slightly later,
        // the surface could already be ready and callback never called
    }

    override fun componentWillUnmount() { //this is called after surfaceDestroyed() making the camera stop before released
        mView.holder.removeCallback(mSurfaceHolderCallback)
        releaseCamera()
    }


    private fun startCamera(props: QRScannerProps) {
        if( !isCameraActive && mView.context != null) { //should never be null here - called from render()
            APromise.on(cameraScheduler).then {
                try {
                    cameraSource.start(mView.holder)
                    isHoldingCamera = true
                    isCameraActive = true

                } catch (e: RuntimeException) {
                    //Exception configuring surface
                    //THINK. So far it happened when it should (e.g. a camera-list-item is recycled)
                    props.initial_onErrorStartingCamera?.also {
                        APromise.postAtEndOfMainExecutionQueue {
                            it.invoke(e)
                        }
                    }
                }
            }.execute(this)
        }
    }

    private fun stopCamera() {
        if (isCameraActive) {
            isCameraActive = false

            if (mView.context != null) {
                APromise.on(cameraScheduler).then {
                    try {
                        cameraSource.stop()
                    } catch (e: Exception) {
                        //some error, assume camera not started
                        e.printStackTrace()
                    }
                }.execute(this)
            }
        }
    }

    private fun releaseCamera() {
        if (isHoldingCamera) {
            if (mView.context != null) {
                APromise.on(cameraScheduler).then {
                    try {
                        cameraSource.release()
                    } catch (e: Exception) {
                        //some error, assume camera released
                        e.printStackTrace()
                    }
                }.execute(this)
            }
            isCameraActive = false
            isHoldingCamera = false
        }
    }



    override fun render() {
        mView.holder.surface?.takeIf { it.isValid }?.also {
            if (ownState.value) { //surface ready
                startCamera(this.props)
            } else {
                stopCamera()
            }
        }
    }
}





fun AComponent<*, *, *>.withQRCodeScanner(@IdRes id: Int) = CQRScanner(mView.findViewById(id))
fun ComponentFragment<*>.withQRCodeScanner(@IdRes id: Int) = CQRScanner(view!!.findViewById(id))
fun ComponentActivity<*>.withQRCodeScanner(@IdRes id: Int) = CQRScanner(findViewById(id))