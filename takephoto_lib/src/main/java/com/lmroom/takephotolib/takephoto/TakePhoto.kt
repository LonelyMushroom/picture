package com.lmroom.takephotolib.takephoto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.lmroom.takephotolib.model.MultipleCrop
import com.lmroom.takephotolib.model.TResult
import com.lmroom.takephotolib.model.TakePhotoOptions
import com.lmroom.takephotolib.permission.PermissionManager
import com.yalantis.ucrop.UCrop

interface TakePhoto {
    /**
     * 图片多选
     * @param limit 最多选择图片张数的限制
     * */
    fun onPickMultiple(limit: Int)

    /**
     * 图片多选并且裁剪
     * @param limit 最多选择图片张数的限制
     * @param options 裁剪配置
     */
    fun onPickMultipleWithCrop(limit:Int,options: UCrop.Options)

    /**
     * 从相机获取图片(不裁剪)
     * @param outPutUri 图片保存的路径
     */
    fun onPickFromCapture(outPutUri: Uri?)

    /**
     * 从相机获取图片并裁剪
     * @param outPutUri 图片裁剪之后保存的路径
     * @param options 裁剪配置
     */
    fun onPickFromCaptureWithCrop(outPutUri: Uri, options: UCrop.Options)

    /**
     * 裁剪图片
     * @param imageUri 要裁剪的图片
     * @param outPutUri 图片裁剪之后保存的路径
     * @param options 裁剪配置
     */
    fun onCrop(imageUri: Uri, outPutUri: Uri, options: UCrop.Options)

    /**
     * 裁剪多张图片
     * @param multipleCrop 要裁切的图片的路径以及输出路径
     * @param options 裁剪配置
     */
    fun onCrop(multipleCrop: MultipleCrop,options: UCrop.Options)

    fun onCreate(savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle?)
    fun setTakePhotoOptions(options: TakePhotoOptions)
    /**
     * 处理拍照或从相册选择的图片或裁剪的结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun permissionNotify(type: PermissionManager.TPermissionType)

    interface TakeResultListener {
        fun takeSuccess(result: TResult)

        fun takeFail(result: TResult, msg: String)

        fun takeCancel()
    }
}