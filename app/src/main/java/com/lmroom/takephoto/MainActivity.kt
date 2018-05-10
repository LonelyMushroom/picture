package com.lmroom.takephoto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.lmroom.takephotolib.model.*
import com.lmroom.takephotolib.permission.InvokeListener
import com.lmroom.takephotolib.permission.PermissionManager
import com.lmroom.takephotolib.permission.TakePhotoInvocationHandler
import com.lmroom.takephotolib.takephoto.TakePhoto
import com.lmroom.takephotolib.takephoto.impl.TakePhotoImpl
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), TakePhoto.TakeResultListener, InvokeListener {
    private var invokeParam: InvokeParam? = null
    private var takePhoto: TakePhoto? = null

    private fun getTakePhoto(): TakePhoto {
        if (takePhoto == null) {
            takePhoto = TakePhotoInvocationHandler.of(this).bind(TakePhotoImpl(this, this)) as TakePhoto
        }
        return takePhoto!!
    }

    override fun takeSuccess(result: TResult) {
        Log.d("1111", "takeSuccess")
//        result.images?.forEach {
//            Glide.with(this@MainActivity)
//                    .load(it.compressPath)
//                    .into(imagePhoto)
//
//        }

        val intent = Intent(this,ImagerActivity::class.java)
        intent.putExtra("images",result.images?: arrayListOf<TImage>())
        startActivity(intent)
    }


    override fun takeFail(result: TResult, msg: String) {
        Log.d("1111", "takeFail")
    }

    override fun takeCancel() {
        Log.d("1111", "takeCancel")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        getTakePhoto().onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getTakePhoto().onActivityResult(requestCode, resultCode, data)
    }

    override fun invoke(invokeParam: InvokeParam?): PermissionManager.TPermissionType {
        val type = PermissionManager.checkPermission(TContextWrap(this), invokeParam?.method!!)
        if (PermissionManager.TPermissionType.WAIT == type) {
            this.invokeParam = invokeParam
        }
        return type
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this)
    }


    lateinit var buttonCapture: Button
    lateinit var btnCaptureCrop: Button
    lateinit var btnMatisse: Button
    lateinit var btnMatisseCrop: Button
    lateinit var imagePhoto: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTakePhoto().onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imagePhoto = find(R.id.img_photo)
        buttonCapture = find(R.id.btn_capture)
        btnCaptureCrop = find(R.id.btn_capture_crop)
        btnMatisse = find(R.id.btn_matisse)
        btnMatisseCrop = find(R.id.btn_matisse_crop)

        val takePhotoOptions = TakePhotoOptions().apply {
            correctImage = true
            enableReserveRaw = true
        }

        val options = UCrop.Options().apply {
            //设置裁剪图片的宽高比，比如16：9
            withAspectRatio(16f, 9f)
            //是否隐藏底部容器，默认显示
            setHideBottomControls(false)
            //是否能调整裁剪框
            setFreeStyleCropEnabled(true)
            ////一共三个参数，分别对应裁剪功能页面的“缩放”，“旋转”，“裁剪”界面，对应的传入NONE，就表示关闭了其手势操作，比如这里我关闭了缩放和旋转界面的手势，只留了裁剪页面的手势操作
            setAllowedGestures(UCropActivity.NONE, UCropActivity.NONE, UCropActivity.ALL)
        }

        buttonCapture.setOnClickListener {
            getTakePhoto().setTakePhotoOptions(takePhotoOptions)
            getTakePhoto().onPickFromCapture(getUri())
        }
        btnCaptureCrop.setOnClickListener {
            getTakePhoto().setTakePhotoOptions(takePhotoOptions)
            // http://wuxiaolong.me/2016/06/20/uCrop/

            getTakePhoto().onPickFromCaptureWithCrop(getUri(), options)
        }

        btnMatisse.setOnClickListener {
            getTakePhoto().setTakePhotoOptions(takePhotoOptions)
            getTakePhoto().onPickMultiple(9)
        }
        btnMatisseCrop.setOnClickListener {
            getTakePhoto().setTakePhotoOptions(takePhotoOptions)
            getTakePhoto().onPickMultipleWithCrop(9,options)
        }
    }

    private fun getUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val imageFileName = "CZCG_JPEG_$timeStamp.png"
        val file = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + "/czcg/" + imageFileName)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        return Uri.fromFile(file)
    }


}