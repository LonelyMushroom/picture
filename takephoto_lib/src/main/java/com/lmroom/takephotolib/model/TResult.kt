package com.lmroom.takephotolib.model

import java.util.*

class TResult {
    var images: ArrayList<TImage>? = null
    var image: TImage? = null

    constructor(images: ArrayList<TImage>?) {
        this.images = images
        if (images != null && images.isEmpty())
            this.image = images[0]
    }

    companion object {
        fun of(images: ArrayList<TImage>?): TResult {
            return TResult(images)
        }

        fun of(image: TImage?): TResult {
            val images = arrayListOf<TImage>()
            if (image != null)
                images.add(image)
            return TResult(images)
        }
    }
}