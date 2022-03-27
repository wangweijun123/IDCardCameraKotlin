package com.bandroid.kyc.camera

import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.bandroid.kyc.camera.SensorControler
import android.hardware.SensorEvent
import com.bandroid.kyc.camera.SensorControler.CameraFocusListener
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.util.Log
import java.util.*

class SensorControler private constructor(context: Context) : SensorEventListener {
    private val mSensorManager: SensorManager
    private val mSensor: Sensor
    private var mX = 0
    private var mY = 0
    private var mZ = 0
    private var lastStaticStamp: Long = 0
    var mCalendar: Calendar? = null
    private var foucsing = 1 //1 表示没有被锁定 0表示被锁定
    var isFocusing = false
    var canFocusIn = false //内部是否能够对焦控制机制
    var canFocus = false
    private var STATUE = STATUS_NONE

    init {
        mSensorManager = context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun onStart() {
        restParams()
        canFocus = true
        mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun onStop() {
        mCameraFocusListener = null
        mSensorManager.unregisterListener(this, mSensor)
        canFocus = false
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == null) {
            return
        }
        if (isFocusing) {
            restParams()
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toInt()
            val y = event.values[1].toInt()
            val z = event.values[2].toInt()
            mCalendar = Calendar.getInstance()
            val stamp = mCalendar!!.getTimeInMillis()
            if (STATUE != STATUS_NONE) {
                val px = Math.abs(mX - x)
                val py = Math.abs(mY - y)
                val pz = Math.abs(mZ - z)
                val value = Math.sqrt((px * px + py * py + pz * pz).toDouble())
                if (value > 1.4) {
                    STATUE = STATUS_MOVE
                } else {
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp
                        canFocusIn = true
                    }
                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELEY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false
                                if (mCameraFocusListener != null) {
                                    mCameraFocusListener!!.onFocus()
                                }
                            }
                        }
                    }
                    STATUE = STATUS_STATIC
                }
            } else {
                lastStaticStamp = stamp
                STATUE = STATUS_STATIC
            }
            mX = x
            mY = y
            mZ = z
        }
    }

    /**
     * 重置参数
     */
    private fun restParams() {
        STATUE = STATUS_NONE
        canFocusIn = false
        mX = 0
        mY = 0
        mZ = 0
    }

    /**
     * 对焦是否被锁定
     *
     * @return
     */
    val isFocusLocked: Boolean
        get() = if (canFocus) {
            foucsing <= 0
        } else false

    /**
     * 锁定对焦
     */
    fun lockFocus() {
        isFocusing = true
        foucsing--
        Log.i(TAG, "lockFocus")
    }

    /**
     * 解锁对焦
     */
    fun unlockFocus() {
        isFocusing = false
        foucsing++
        Log.i(TAG, "unlockFocus")
    }

    fun restFoucs() {
        foucsing = 1
    }

    private var mCameraFocusListener: CameraFocusListener? = null

    interface CameraFocusListener {
        fun onFocus()
    }

    fun setCameraFocusListener(mCameraFocusListener: CameraFocusListener?) {
        this.mCameraFocusListener = mCameraFocusListener
    }

    companion object {
        const val TAG = "SensorControler"
        const val DELEY_DURATION = 500
        private var mInstance: SensorControler? = null
        const val STATUS_NONE = 0
        const val STATUS_STATIC = 1
        const val STATUS_MOVE = 2
        @JvmStatic
        fun getInstance(context: Context): SensorControler? {
            if (mInstance == null) {
                mInstance = SensorControler(context)
            }
            return mInstance
        }
    }
}