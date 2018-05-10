package com.lmroom.takephoto.util

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

object IntentUtils{
    /**
     * 获取拍照的Intent
     */
    fun getCaptureIntent(outPutUri: Uri): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT,outPutUri)
        return intent
    }

    /**
     * 获取选择照片的Intent
     */
    fun getPickIntentWithGallery(): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        return intent
    }

    /**
     * 获取从文件中选择照片的Intent
     */
    fun getPickIntentWithDocuments(): Intent {
        val intent = Intent()
        intent.type = "image/*"
        return intent
    }
}