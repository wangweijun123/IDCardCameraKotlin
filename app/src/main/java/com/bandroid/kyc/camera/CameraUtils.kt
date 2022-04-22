package com.bandroid.kyc.camera

import android.hardware.Camera
import android.util.Log
import java.lang.Exception

object CameraUtils {
    var camera: Camera? = null
    var isFront = false

    /**
     * 打开相机
     *
     * @return
     */
    @JvmStatic
    fun openCamera(): Camera? {
        camera = null
        isFront = false
        try {
            val numberOfCameras = Camera.getNumberOfCameras()
            Log.d(CameraPreview.TAG, "numberOfCameras = $numberOfCameras")
            if (isFront) {
                camera = Camera.open(1) // 0 后置 1 前置
            } else {
                camera = Camera.open(0) // 0 后置 1 前置
            }

        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
        }
        return camera // returns null if camera is unavailable
    }
}