package com.lmroom.takephotolib.model

import android.app.Activity
import android.net.Uri
import com.lmroom.takephoto.util.TImageFiles
import com.lmroom.takephoto.util.TUtils

class MultipleCrop {
    var uris: ArrayList<Uri>
    var outUris: ArrayList<Uri>
    var tImages: ArrayList<TImage>
    var fromType: TImage.FromType

    var hasFailed: Boolean = false

    constructor(uris: ArrayList<Uri>, activity: Activity, fromType: TImage.FromType) {
        this.uris = uris
        val outUris = arrayListOf<Uri>()
        uris.forEach {
            outUris.add(Uri.fromFile(TImageFiles.getTempFile(activity, it)))
        }
        this.outUris = outUris
        this.tImages = TUtils.getTImagesWithUris(outUris, fromType)
        this.fromType = fromType
    }

    constructor(uris: ArrayList<Uri>, outUris: ArrayList<Uri>, fromType: TImage.FromType) {
        this.uris = uris
        this.outUris = outUris
        this.tImages = TUtils.getTImagesWithUris(outUris, fromType)
        this.fromType = fromType
    }

    fun setCropWithUri(uri: Uri, cropped: Boolean): HashMap<String, Any> {
        if (!cropped) hasFailed = true
        val index = outUris.indexOf(uri)
        tImages[index].cropped = cropped
        val result = hashMapOf<String, Any>()
        result["index"] = index
        result["isLast"] = index == outUris.size - 1
        return result
    }
}