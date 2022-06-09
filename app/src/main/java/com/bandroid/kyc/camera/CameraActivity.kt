package com.bandroid.kyc.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bandroid.kyc.R
import com.bandroid.kyc.camera.utils.ImageUtils
import com.bandroid.kyc.camera.utils.PermissionUtils
import java.io.File


class CameraActivity : Activity(), View.OnClickListener {
    private var cameraTitle: String? = null
    private var cameraSubtitle: String? = null
    private var targetImgUri: String? = null
    private var cameraFileType: String? = null
    private var mCameraPreview: CameraPreview? = null
    private var mIvCameraCrop: ImageView? = null
    private var mCropBitmap: Bitmap? = null
    private var leftMock: View? = null
    private var headerMock: View? = null
    private var cameraTitleTV: TextView? = null
    private var cameraSubTitleTV: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*动态请求需要的权限*/
        val checkPermissionFirst = PermissionUtils.checkPermissionFirst(
            this, PERMISSION_CODE_FIRST, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
        if (checkPermissionFirst) {
            init()
        }
    }

    private fun init() {
        setContentView(R.layout.activity_camera)
        initView()
        initListener()
    }

    private fun initView() {
        mCameraPreview = findViewById<View>(R.id.camera_preview) as CameraPreview
        Log.d(CameraPreview.TAG, "initView tid = ${Thread.currentThread().id}, ${Thread.currentThread().name}")
        mIvCameraCrop = findViewById<View>(R.id.cropIv) as ImageView
        leftMock = findViewById(R.id.leftMock)
        headerMock = findViewById(R.id.headerMock)
        cameraTitleTV = findViewById(R.id.camera_title)
        cameraSubTitleTV = findViewById(R.id.camera_subtitle)

        /*增加0.5秒过渡界面，解决个别手机首次申请权限导致预览界面启动慢的问题*/
        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                mCameraPreview!!.visibility = View.VISIBLE
            }
        }, 500)

        mCameraPreview!!.openCameraFailedCallback = {
            Log.d(CameraPreview.TAG, "CameraActivity open camera failed")
        }
    }

    private fun initListener() {
        findViewById<View>(R.id.iv_camera_take).setOnClickListener(this)
        mCameraPreview!!.setOnClickListener(this)
        intent?.let {
            cameraTitle = it.getStringExtra(KEY_CAMERA_TITLE)
            cameraSubtitle = it.getStringExtra(KEY_CAMERA_SUBTITLE)
            targetImgUri = it.getStringExtra(KEY_IMG_URI)
            cameraFileType = it.getStringExtra(KEY_CAMERA_FILE_TYPE)
            cameraTitleTV!!.text = cameraTitle
            cameraSubTitleTV!!.text = cameraSubtitle
//            if ("1" == cameraFileType) {
//                mIvCameraCrop!!.setImageResource(R.drawable.camera_passport_front)
//            } else {
//                mIvCameraCrop!!.setImageResource(R.drawable.camera_idcard_front)
//            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.camera_preview -> mCameraPreview!!.focus()
            R.id.iv_camera_take -> takePhoto()
            else -> {}
        }
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        mCameraPreview!!.isEnabled = false
        CameraUtils.camera?.setOneShotPreviewCallback { bytes, camera ->
            val size = camera.parameters.previewSize //获取预览大小 height:1440 width:3200
            camera.stopPreview()
            Thread {
                val metric = DisplayMetrics()
                windowManager.defaultDisplay.getRealMetrics(metric)
                val width = metric.widthPixels // 宽度（PX）
                val height = metric.heightPixels // 高度（PX）
                Log.d(CameraPreview.TAG, "手机分辨率大小 width=$width, height=$height")

                val w = size.width
                val h = size.height
                Log.d(CameraPreview.TAG, "获取预览大小 W=$w, h=$h")
                val bigBitmap = ImageUtils.getBitmapFromByte(bytes, w, h)
                bigBitmap?.let {
                    Log.d(CameraPreview.TAG, "拍照返回的预览的字节数组生成bitmap，这是一张大的图片")
                    val bigFile = ImageUtils.createFile(
                        ImageUtils.getOutputDirectory(applicationContext),
                        FILENAME, PHOTO_EXTENSION
                    )
                    val success = ImageUtils.saveBigImage(bigFile, it)
                    Log.d(CameraPreview.TAG, "保存big bitmap success ? $success")
                    cropImage(it)
                }
            }.start()
        }
    }

    /**
     * 裁剪图片
     */
    private fun cropImage(bitmap: Bitmap) {
        Log.d(CameraPreview.TAG, "大图大小 width=" + bitmap.width + ", height=" + bitmap.height)
        val bitmapRotate = rotateImage(bitmap, 90)
        Log.d(
            CameraPreview.TAG,
            "旋转后大图大小 width=" + bitmapRotate.width + ", height=" + bitmapRotate.height
        )
        val previewWidth = mCameraPreview!!.width
        val previewHeight = mCameraPreview!!.height
        Log.d(CameraPreview.TAG, "预览大小 previewWidth=$previewWidth, previewHeight=$previewHeight")

        // 扫描框区域位置
        val cropLeft = leftMock!!.width
        val cropTop = headerMock!!.height
        val cropRight = cropLeft + mIvCameraCrop!!.width
        val cropBottom = cropTop + mIvCameraCrop!!.height
        Log.d(
            CameraPreview.TAG, "裁剪区域位置 cropLeft=" + cropLeft + ", cropRight=" + cropRight +
                    ", cropTop=" + cropTop + ", cropBottom=" + cropBottom
        )
        val leftProportion = cropLeft / previewWidth.toFloat()
        val topProportion = cropTop / previewHeight.toFloat()
        val rightProportion = cropRight / previewWidth.toFloat()
        val bottomProportion = cropBottom / previewHeight.toFloat()
        Log.d(
            CameraPreview.TAG, "计算扫描框坐标点占原图坐标点的比例 leftProportion:" + leftProportion
                    + ", topProportion:" + topProportion + ", rightProportion:"
                    + rightProportion + ", bottomProportion:" + bottomProportion
        )
        val x = (leftProportion * bitmapRotate.width).toInt()
        val y = (topProportion * bitmapRotate.height).toInt()
        val cropWidth = ((rightProportion - leftProportion) * bitmapRotate.width).toInt()
        val cropHeight = ((bottomProportion - topProportion) * bitmapRotate.height).toInt()
        Log.d(CameraPreview.TAG, "x=$x, y=$y, cropWidth=$cropWidth, cropHeight=$cropHeight")
        mCropBitmap = Bitmap.createBitmap(bitmapRotate, x, y, cropWidth, cropHeight)
        var cropfile = File(targetImgUri)
        Log.d(CameraPreview.TAG, "传过来的file exists ? " + cropfile.exists())
        if (!cropfile.exists()) {
            cropfile = ImageUtils.createFile(
                ImageUtils.getOutputDirectory(applicationContext),
                FILENAME, PHOTO_EXTENSION
            )
        }
        mCropBitmap?.let {
            val success = ImageUtils.saveBigImage(cropfile, it)
            Log.d(CameraPreview.TAG, "保存裁剪后的图片 success ? $success")
            runOnUiThread {
                mIvCameraCrop!!.setImageBitmap(it)
                val intent = Intent()
                intent.putExtra("image_path", cropfile.absolutePath)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mCameraPreview != null) {
            mCameraPreview!!.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mCameraPreview != null) {
            mCameraPreview!!.onStop()
        }
    }

    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isPermissions = true
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false
                ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])
            }
        }
        if (isPermissions) {
            init()
        } else {
            finish()
        }
    }

    companion object {
        private const val PERMISSION_CODE_FIRST = 1
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PHOTO_EXTENSION = ".jpg"
        private const val KEY_CAMERA_TITLE = "camera_title"
        private const val KEY_CAMERA_SUBTITLE = "camera_subtitle"
        private const val KEY_IMG_URI = "img_uri"
        private const val KEY_CAMERA_FILE_TYPE = "camera_file_type"
        fun rotateImage(imageToOrient: Bitmap, degreesToRotate: Int): Bitmap {
            var result = imageToOrient
            try {
                if (degreesToRotate != 0) {
                    val matrix = Matrix()
                    matrix.setRotate(degreesToRotate.toFloat())
                    result = Bitmap.createBitmap(imageToOrient,0,0,imageToOrient.width,imageToOrient.height,matrix,true)
                }
            } catch (ex: Exception) {
                Log.e("TransformationUtils", "Exception when trying to orient image", ex)
            }
            return result
        }
    }

    private fun testlifecycleScope() {
//        lifecycleScope
    }
}