package com.bandroid.kyc.camera

import android.hardware.Camera
import java.lang.Exception

object CameraUtils {
    var camera: Camera? = null


    /**
     * 打开相机
     *
     * @return
     */
    @JvmStatic
    fun openCamera(): Camera? {
        camera = null
        try {
            camera = Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
        }
        return camera // returns null if camera is unavailable
    }
}