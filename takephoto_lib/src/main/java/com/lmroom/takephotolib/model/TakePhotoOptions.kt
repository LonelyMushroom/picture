package com.lmroom.takephotolib.model

import java.io.Serializable

class TakePhotoOptions : Serializable {
    /**
     * 是否使用TakePhoto自带的相册进行图片选择，默认不使用，但选择多张图片会使用
     */
    var withOwnGallery: Boolean = false
    /**
     * 是对拍的照片进行旋转角度纠正
     */
    var correctImage: Boolean = true

    /**
     * 是否保留原文件
     */
    var enableReserveRaw:Boolean = true
}