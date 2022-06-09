package com.bandroid.kyc

import com.bandroid.kyc.camera.utils.MyImageUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        val outWidth = 1093; //re =1
//        val outHeight = 554

//        val outWidth = 1992; //re =1
//        val outHeight = 1053

//        val outWidth = 3500; //re =2
//        val outHeight = 2500

        val outWidth = 500; //re =1
        val outHeight = 300

        val reqWidth = 1000
        val reqHeight = 1000

        val re = MyImageUtils.calculateInSampleSize(outWidth,outHeight, reqWidth, reqHeight)
        println("re = $re")
    }
}