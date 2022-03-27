package com.bandroid.kyc.utils

import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.util.ArrayList

object PermissionUtils {
    /**
     * 第一次检查权限，用在打开应用的时候请求应用需要的所有权限
     *
     * @param context
     * @param requestCode 请求码
     * @param permission  权限数组
     * @return
     */
    fun checkPermissionFirst(
        context: Context?,
        requestCode: Int,
        permission: Array<String>
    ): Boolean {
        val permissions: MutableList<String> = ArrayList()
        for (per in permission) {
            val permissionCode = ActivityCompat.checkSelfPermission(context!!, per)
            if (permissionCode != PackageManager.PERMISSION_GRANTED) {
                permissions.add(per)
            }
        }
        return if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!,
                permissions.toTypedArray(),
                requestCode
            )
            false
        } else {
            true
        }
    }
}