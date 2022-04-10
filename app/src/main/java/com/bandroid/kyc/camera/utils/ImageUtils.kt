package com.bandroid.kyc.camera.utils

import android.content.Context
import android.graphics.*
import com.bandroid.kyc.camera.utils.FileUtils.getFileByPath
import com.bandroid.kyc.camera.utils.FileUtils.createOrExistsFile
import com.bandroid.kyc.camera.utils.FileUtils.closeIO
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import kotlin.jvm.JvmOverloads
import com.bandroid.kyc.camera.CameraPreview
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    /**
     * 保存图片
     *
     * @param src      源图片
     * @param filePath 要保存到的文件路径
     * @param format   格式
     * @return `true`: 成功<br></br>`false`: 失败
     */
    fun save(src: Bitmap, filePath: String?, format: CompressFormat?): Boolean {
        return save(src, getFileByPath(filePath), format, false)
    }

    /**
     * 保存图片
     *
     * @param src      源图片
     * @param filePath 要保存到的文件路径
     * @param format   格式
     * @param recycle  是否回收
     * @return `true`: 成功<br></br>`false`: 失败
     */
    fun save(src: Bitmap, filePath: String?, format: CompressFormat?, recycle: Boolean): Boolean {
        return save(src, getFileByPath(filePath), format, recycle)
    }
    /**
     * 保存图片
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return `true`: 成功<br></br>`false`: 失败
     */
    /**
     * 保存图片
     *
     * @param src    源图片
     * @param file   要保存到的文件
     * @param format 格式
     * @return `true`: 成功<br></br>`false`: 失败
     */
    @JvmOverloads
    fun save(src: Bitmap, file: File?, format: CompressFormat?, recycle: Boolean = false): Boolean {
        if (isEmptyBitmap(src) || !createOrExistsFile(file)) {
            return false
        }
        println(src.width.toString() + ", " + src.height)
        var os: OutputStream? = null
        var ret = false
        try {
            os = BufferedOutputStream(FileOutputStream(file))
            ret = src.compress(format, 100, os)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            closeIO(os)
        }
        return ret
    }

    /**
     * 判断bitmap对象是否为空
     *
     * @param src 源图片
     * @return `true`: 是<br></br>`false`: 否
     */
    private fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }

    /**
     * 将byte[]转换成Bitmap
     *
     * @param bytes
     * @param width
     * @param height
     * @return
     */
    fun getBitmapFromByte(bytes: ByteArray, width: Int, height: Int): Bitmap? {
        val image = YuvImage(bytes, ImageFormat.NV21, width, height, null)
        val os = ByteArrayOutputStream(bytes.size)
        if (!image.compressToJpeg(Rect(0, 0, width, height), 100, os)) {
            return null
        }
        val tmp = os.toByteArray()
        return BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
    }

    fun saveBigImage(file: File, bitmap: Bitmap): Boolean {
        Log.d(CameraPreview.TAG, "图片地址::" + file.absolutePath)
        return save(bitmap, file.absolutePath, CompressFormat.JPEG)
    }

    @JvmStatic
    fun createFile(baseFolder: File?, format: String?, extension: String): File {
        val simpleDateFormat = SimpleDateFormat(format, Locale.US)
        return File(baseFolder, simpleDateFormat.format(System.currentTimeMillis()) + extension)
    }

    @JvmStatic
    fun getOutputDirectory(context: Context): File {
        val externalMediaDirs = context.externalMediaDirs
        return if (externalMediaDirs != null && externalMediaDirs.size > 0) {
            val externalMediaDir = externalMediaDirs[0]
            Log.d(CameraPreview.TAG, "externalMediaDir= " + externalMediaDir.absolutePath)
            val imageCacheDir = File(externalMediaDir, "imagecache")
            if (!imageCacheDir.exists()) {
                imageCacheDir.mkdirs()
            }
            imageCacheDir
        } else {
            context.filesDir
        }
    }
}