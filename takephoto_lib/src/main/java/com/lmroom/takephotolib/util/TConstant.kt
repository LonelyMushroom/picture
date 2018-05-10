package com.lmroom.takephoto.util

import android.content.Context


/**
 * 常量类
 */
object TConstant {

    /**
     * request Code 裁剪照片
     */
    const val RC_CROP = 1001

    /**
     * request Code 从相机获取照片并裁剪
     */
    const val RC_PICK_PICTURE_FROM_CAPTURE_CROP = 1002

    /**
     * request Code 从相机获取照片不裁剪
     */
    const val RC_PICK_PICTURE_FROM_CAPTURE = 1003

    /**
     * request Code 选择多张照片
     */
    const val RC_PICK_MULTIPLE = 1008

    /**
     * requestCode 请求权限
     */
    const val PERMISSION_REQUEST_TAKE_PHOTO = 2000

    fun getFileProviderName(context: Context): String {
        return context.packageName + ".fileprovider"
    }
}