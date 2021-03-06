package com.bandroid.kyc.camera

import com.bandroid.kyc.camera.SensorControler.Companion.getInstance
import com.bandroid.kyc.camera.CameraUtils.openCamera
import com.bandroid.kyc.camera.utils.ScreenUtils.getScreenWidth
import com.bandroid.kyc.camera.utils.ScreenUtils.getScreenHeight
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import com.bandroid.kyc.camera.SensorControler.CameraFocusListener
import java.lang.Exception

class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var camera: Camera? = null
    private var mSensorControler: SensorControler? = null
    private var mContext: Context? = null
    private var mSurfaceHolder: SurfaceHolder? = null

    var openCameraFailedCallback: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        mSurfaceHolder = holder
        mSurfaceHolder?.let {
            it.addCallback(this)
            it.setKeepScreenOn(true)
            it.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
        mSensorControler = getInstance(context.applicationContext)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated tid = ${Thread.currentThread().id}, ${Thread.currentThread().name}")
            camera = openCamera()
        Log.d(TAG, "openCameraFailedCallback?.invoke ...")
        openCameraFailedCallback?.invoke()
        camera?.let {
            try {
                it.setPreviewDisplay(holder)
                val parameters = it.parameters
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //????????????????????????????????????90???????????????????????????????????????????????????????????????
                    it.setDisplayOrientation(90)
                    parameters.setRotation(90)
                } else {
                    it.setDisplayOrientation(0)
                    parameters.setRotation(0)
                }
                val sizeList = parameters.supportedPreviewSizes //?????????????????????????????????
                val bestSize = getOptimalPreviewSize(
                    sizeList, getScreenWidth(mContext!!), getScreenHeight(mContext!!)
                )
                bestSize?.let {
                    parameters.setPreviewSize(it.width, it.height) //??????????????????
                    Log.d(TAG, "??????????????????????????????????????????????????????????????????????????????width="
                            + bestSize.width + ", height=" + bestSize.height)
                }
                it.parameters = parameters
                it.startPreview()
                focus() //????????????
            } catch (e: Exception) {
                Log.d(TAG, "Error setting camera preview: " + e.message)
                try {
                    val parameters = it.parameters
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        it.setDisplayOrientation(90)
                        parameters.setRotation(90)
                    } else {
                        it.setDisplayOrientation(0)
                        parameters.setRotation(0)
                    }
                    it.parameters = parameters
                    it.startPreview()
                    focus() //????????????
                } catch (e1: Exception) {
                    e.printStackTrace()
                    camera = null
                }
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param sizes ???????????????????????????
     * @param w     SurfaceView???
     * @param h     SurfaceView???
     * @return
     */
    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val targetRatio = w.toDouble() / h
        if (sizes == null) return null
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        // Try to find an size match aspect ratio and size
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > 0.1) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        //????????????????????????????????????????????????????????????????????????????????????
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        holder.removeCallback(this)
        //??????????????????
        release()
    }

    /**
     * ????????????
     */
    private fun release() {
        camera?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
            camera = null
        }
    }

    /**
     * ????????????CameraActivity?????????????????????????????????
     */
    fun focus() {
        camera?.apply {
            try {
                autoFocus(null)
            } catch (e: Exception) {
                Log.d(TAG, "takePhoto $e")
            }
        }
    }

    fun startPreview() {
        camera?.apply {
            startPreview()
        }
    }

    fun onStart() {
        addCallback()
        mSensorControler?.apply {
            onStart()
            setCameraFocusListener(object : CameraFocusListener {
                override fun onFocus() {
                    focus()
                }
            })
        }
    }

    fun onStop() {
        mSensorControler?.apply {
            onStop()
        }
    }

    private fun addCallback() {
        mSurfaceHolder?.also {
            it.addCallback(this)
        }
    }

    companion object {
        @JvmField
        var TAG = CameraPreview::class.java.name
    }
}