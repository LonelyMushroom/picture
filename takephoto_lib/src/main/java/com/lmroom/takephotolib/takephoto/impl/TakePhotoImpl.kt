package com.lmroom.takephotolib.takephoto.impl

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.lmroom.takephoto.util.*
import com.lmroom.takephotolib.R
import com.lmroom.takephotolib.compress.CompressImage
import com.lmroom.takephotolib.compress.impl.CompressWithLuBan
import com.lmroom.takephotolib.model.*
import com.lmroom.takephotolib.permission.PermissionManager
import com.lmroom.takephotolib.takephoto.TakePhoto
import com.yalantis.ucrop.UCrop
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import java.io.File


class TakePhotoImpl : TakePhoto {

    private val SOURCE_CAMERA = "camera"
    private val SOURCE_MULTI_SELECT = "multiSelect"

    private lateinit var contextWrap: TContextWrap
    private var listener: TakePhoto.TakeResultListener? = null
    private var outPutUri: Uri? = null
    private var tempUri: Uri? = null
    private lateinit var fromType: TImage.FromType
    private var cropOptions: UCrop.Options? = null
    private var multipleCrop: MultipleCrop? = null

    private var takePhotoOption: TakePhotoOptions? = null
    private lateinit var permissionType: PermissionManager.TPermissionType


    private fun takeResult(result: TResult, vararg message: String) {
        if (takePhotoOption?.enableReserveRaw == false) {
            handleTakeCallBack(result, *message)
        } else {
            CompressWithLuBan(contextWrap.activity, result.images!!, object : CompressImage.CompressListener {
                override fun onCompressSuccess(images: ArrayList<TImage>) {
                    takePhotoOption?.let {
                        if (!it.enableReserveRaw) {
                            deleteRawFile(images)
                        }
                    }
                    handleTakeCallBack(result)
                }

                override fun onCompressFailed(images: ArrayList<TImage>?, msg: String) {
                    takePhotoOption?.let {
                        if (!it.enableReserveRaw) {
                            deleteRawFile(images)
                        }
                    }
                    handleTakeCallBack(TResult.of(images),
                            String.format(contextWrap.activity.resources.getString(R.string.tip_compress_failed),
                                    if (message.isNotEmpty()) message[0] else "", msg, result.image?.compressPath))
                }
            }).compress()
        }
    }

    private fun deleteRawFile(images: ArrayList<TImage>?) {
        images?.forEach {
            if (it.fromType == TImage.FromType.CAMERA) {
                TFileUtils.delete(it.originalPath)
                it.originalPath = ""
            }
        }
    }

    private fun handleTakeCallBack(result: TResult, vararg message: String) {
        if (message.isNotEmpty()) {
            listener?.takeFail(result, message[0])
        } else if (takePhotoOption?.enableReserveRaw != false) {
            var hasFailed = false
            result.images?.forEach {
                if (it.compressed == false) {
                    hasFailed = true
                    return@forEach
                }
            }
            if (hasFailed) {
                listener?.takeFail(result, contextWrap.activity.getString(R.string.msg_compress_failed))
            } else {
                listener?.takeSuccess(result)
            }
        } else {
            listener?.takeSuccess(result)
        }
        clearParams()
    }

    private fun cropContinue(preSuccess: Boolean) {
        multipleCrop?.apply {
            val map = setCropWithUri(outPutUri!!, preSuccess)
            val index = map["index"] as Int
            val isLast = map["isLast"] as Boolean
            if (isLast) {
                if (preSuccess) {
                    takeResult(TResult.of(multipleCrop?.tImages))
                } else {
                    takeResult(TResult.of(multipleCrop?.tImages), outPutUri?.path + contextWrap.activity.resources.getString(R.string.msg_crop_canceled))
                }
            } else {
                val uri = multipleCrop?.uris?.get(index + 1)
                val outUri = multipleCrop?.outUris?.get(index + 1)
                cropWithNonException(uri!!, outUri!!, cropOptions!!)
            }
        }
    }

    fun cropWithNonException(imageUri: Uri, outPutUri: Uri, options: UCrop.Options) {
        this.outPutUri = outPutUri
        UCrop.of(imageUri, outPutUri)
                .withOptions(options)
                .start(contextWrap.activity)
    }

    private fun clearParams() {
        takePhotoOption = null
        multipleCrop = null
        cropOptions = null
    }

    constructor(activity: Activity, listener: TakePhoto.TakeResultListener) {
        this.contextWrap = TContextWrap(activity)
        this.listener = listener
    }

    constructor(fragment: Fragment, listener: TakePhoto.TakeResultListener) {
        this.contextWrap = TContextWrap(fragment)
        this.listener = listener
    }


    override fun setTakePhotoOptions(options: TakePhotoOptions) {
        this.takePhotoOption = options
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            outPutUri = savedInstanceState.getParcelable("outPutUri")
            tempUri = savedInstanceState.getParcelable("tempUri")

            takePhotoOption = savedInstanceState.getSerializable("takePhotoOptions") as TakePhotoOptions?
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable("outPutUri", outPutUri)
        outState?.putParcelable("tempUri", tempUri)

        outState?.putSerializable("takePhotoOptions", takePhotoOption)
    }

    override fun permissionNotify(type: PermissionManager.TPermissionType) {
        this.permissionType = type
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
        // 拍照
            TConstant.RC_PICK_PICTURE_FROM_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val uri = TUriParse.getFilePathWithUri(outPutUri, contextWrap.activity)
                        val tImage = TImage.of(uri, fromType)
                        val tResult = TResult.of(tImage)
                        takeResult(tResult)
                    } catch (e: TException) {
                        takeResult(TResult.of(TImage.of(outPutUri!!, fromType)), e.detailMessage)
                        e.printStackTrace()
                    }
                } else {
                    listener?.takeCancel()
                }
            }
        // 拍照并且裁剪
            TConstant.RC_PICK_PICTURE_FROM_CAPTURE_CROP -> {
                if (resultCode == Activity.RESULT_OK) {
                    takePhotoOption?.correctImage.let {
                        ImageRotateUtil.correctImage(contextWrap.activity, tempUri!!)
                    }
                    try {
                        val uri = Uri.fromFile(File(TUriParse.parseOwnUri(contextWrap.activity, outPutUri)))
                        onCrop(tempUri!!, uri, cropOptions!!)
                    } catch (e: TException) {
                        takeResult(TResult.of(TImage.of(outPutUri!!, fromType)), e.detailMessage)
                    }
                } else {
                    listener?.takeCancel()
                }
            }
        // 相册多选 裁剪/没裁剪
            TConstant.RC_PICK_MULTIPLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val paths: List<String> = Matisse.obtainPathResult(data)
                    if (cropOptions != null) {
                        try {
                            val uris: ArrayList<Uri> = arrayListOf()
                            paths.forEach {
                                uris.add(Uri.fromFile(File(it)))
                            }
                            val multipleCrop2Crop = MultipleCrop(uris, contextWrap.activity, TImage.FromType.OTHER)
                            onCrop(multipleCrop2Crop, cropOptions!!)
                        } catch (e: TException) {
                            cropContinue(false)
                            e.printStackTrace()
                        }
                    } else {
                        val tImages: ArrayList<TImage> = arrayListOf()
                        paths.forEach {
                            tImages.add(TImage.of(it, TImage.FromType.OTHER))
                        }
                        takeResult(TResult.of(tImages))
                    }
                }
            }
        // 裁剪 多张图片/单张图片
            UCrop.REQUEST_CROP -> {
                if (resultCode == RESULT_OK) {
                    if (multipleCrop != null) {
                        cropContinue(true)
                    } else {
                        try {
                            val filePathWithUri = TUriParse.getFilePathWithUri(outPutUri!!, contextWrap.activity)
                            val tImage = TImage.of(filePathWithUri, fromType)
                            tImage.cropped = true
                            takeResult(TResult.of(tImage))
                        } catch (e: TException) {
                            val tImage = TImage.of(outPutUri?.path!!, fromType)
                            takeResult(TResult.of(tImage), e.detailMessage)
                            e.printStackTrace()
                        }
                    }
                } else {
                    listener?.takeCancel()
                }
            }
        // 裁剪失败
            UCrop.RESULT_ERROR -> {
                throw UCrop.getError(data!!)!!
            }
        }
    }

    override fun onPickMultiple(limit: Int) {
        this.fromType = TImage.FromType.OTHER
        Matisse.from(contextWrap.activity)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(limit)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(GlideEngine())
                .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                .forResult(TConstant.RC_PICK_MULTIPLE)
    }

    override fun onPickMultipleWithCrop(limit: Int, options: UCrop.Options) {
        this.cropOptions = options
        onPickMultiple(limit)
    }

    override fun onPickFromCapture(outPutUri: Uri?) {
        this.fromType = TImage.FromType.CAMERA
        if (PermissionManager.TPermissionType.WAIT == permissionType) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.outPutUri = TUriParse.convertFileUriToFileProviderUri(contextWrap.activity, outPutUri)
        } else {
            this.outPutUri = outPutUri
        }

        try {
            if (this.outPutUri != null) {
                val captureIntent = IntentUtils.getCaptureIntent(this.outPutUri!!)
                val tIntentWrap = TIntentWrap(captureIntent, TConstant.RC_PICK_PICTURE_FROM_CAPTURE)
                TUtils.captureBySafely(contextWrap, tIntentWrap)
            }
        } catch (e: TException) {
            takeResult(TResult.of(TImage.of("", fromType)), e.detailMessage)
            e.printStackTrace()
        }
    }


    override fun onPickFromCaptureWithCrop(outPutUri: Uri, options: UCrop.Options) {
        this.fromType = TImage.FromType.CAMERA
        if (PermissionManager.TPermissionType.WAIT == permissionType) return
        this.cropOptions = options
        this.outPutUri = outPutUri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.tempUri = TUriParse.getTempUri(contextWrap.activity)
        } else {
            this.tempUri = outPutUri
        }
        try {
            TUtils.captureBySafely(contextWrap, TIntentWrap(IntentUtils.getCaptureIntent(this.tempUri!!),
                    TConstant.RC_PICK_PICTURE_FROM_CAPTURE_CROP))
        } catch (e: TException) {
            takeResult(TResult.of(TImage.of("", fromType)), e.detailMessage)
            e.printStackTrace()
        }
    }

    override fun onCrop(imageUri: Uri, outPutUri: Uri, options: UCrop.Options) {
        if (PermissionManager.TPermissionType.WAIT == permissionType) return
        this.outPutUri = outPutUri
        if (!TImageFiles.checkMimeType(contextWrap.activity, TImageFiles.getMimeType(contextWrap.activity, imageUri))) {
            Toast.makeText(contextWrap.activity, contextWrap.activity.resources.getText(R.string.tip_type_not_image), Toast.LENGTH_SHORT).show()
            throw TException(TExceptionType.TYPE_NOT_IMAGE)
        }
        cropWithNonException(imageUri, outPutUri, options)
    }

    override fun onCrop(multipleCrop: MultipleCrop, options: UCrop.Options) {
        this.multipleCrop = multipleCrop
        this.cropOptions = options
        onCrop(multipleCrop.uris[0], multipleCrop.outUris[0], options)

    }
}