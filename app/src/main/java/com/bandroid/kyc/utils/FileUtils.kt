package com.bandroid.kyc.utils

import android.content.Context
import android.os.Environment
import java.io.Closeable
import java.io.File
import java.io.IOException

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/10
 * Desc	        ${文件相关工具类}
 */
object FileUtils {//内部存储的根目录    /data//SD卡根目录    /storage/emulated/0
    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 文件路径
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    fun createOrExistsDir(dirPath: String?): Boolean {
        return createOrExistsDir(getFileByPath(dirPath))
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    fun createOrExistsDir(file: File?): Boolean {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param filePath 文件路径
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    fun createOrExistsFile(filePath: String?): Boolean {
        return createOrExistsFile(getFileByPath(filePath))
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    @JvmStatic
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    @JvmStatic
    fun getFileByPath(filePath: String?): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    /**
     * 判断字符串是否为 null 或全为空白字符
     *
     * @param s
     * @return
     */
    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    @JvmStatic
    fun closeIO(vararg closeables: Closeable?) {
        if (closeables == null) return
        try {
            for (closeable in closeables) {
                closeable?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 获取缓存图片的目录
     *
     * @param context Context
     * @return 缓存图片的目录
     */
    fun getImageCacheDir(context: Context): String {
        val file: File?
        file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        } else {
            context.cacheDir
        }
        val path = file!!.path + "/cache"
        val cachePath = File(path)
        if (!cachePath.exists()) cachePath.mkdir()
        return path
    }

    /**
     * 删除缓存图片目录中的全部图片
     *
     * @param context
     */
    fun clearCache(context: Context) {
        val cacheImagePath = getImageCacheDir(context)
        val cacheImageDir = File(cacheImagePath)
        val files = cacheImageDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    file.delete()
                }
            }
        }
    }
}