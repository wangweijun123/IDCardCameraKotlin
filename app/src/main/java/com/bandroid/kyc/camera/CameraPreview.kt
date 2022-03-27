package com.bandroid.kyc.camera

import com.bandroid.kyc.camera.SensorControler.Companion.getInstance
import com.bandroid.kyc.camera.CameraUtils.openCamera
import com.bandroid.kyc.utils.ScreenUtils.getScreenWidth
import com.bandroid.kyc.utils.ScreenUtils.getScreenHeight
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
        camera = openCamera()
        camera?.let {
            try {
                it.setPreviewDisplay(holder)
                val parameters = it.parameters
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏拍照时，需要设置旋转90度，否者看到的相机预览方向和界面方向不相同
                    it.setDisplayOrientation(90)
                    parameters.setRotation(90)
                } else {
                    it.setDisplayOrientation(0)
                    parameters.setRotation(0)
                }
                val sizeList = parameters.supportedPreviewSizes //获取所有支持的预览大小
                val bestSize = getOptimalPreviewSize(
                    sizeList, getScreenWidth(mContext!!), getScreenHeight(mContext!!)
                )
                bestSize?.let {
                    parameters.setPreviewSize(it.width, it.height) //设置预览大小
                    Log.d(TAG, "获取所有支持的预览大小，根据屏幕宽高获取最佳预览大小width="
                            + bestSize.width + ", height=" + bestSize.height)
                }
                it.parameters = parameters
                it.startPreview()
                focus() //首次对焦
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
                    focus() //首次对焦
                } catch (e1: Exception) {
                    e.printStackTrace()
                    camera = null
                }
            }
        }
    }

    /**
     * 获取最佳预览大小
     *
     * @param sizes 所有支持的预览大小
     * @param w     SurfaceView宽
     * @param h     SurfaceView高
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
        //因为设置了固定屏幕方向，所以在实际使用中不会触发这个方法
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        holder.removeCallback(this)
        //回收释放资源
        release()
    }

    /**
     * 释放资源
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
     * 对焦，在CameraActivity中触摸对焦或者自动对焦
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