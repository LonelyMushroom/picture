package com.lmroom.takephotolib.model

import android.net.Uri
import java.io.Serializable

class TImage : Serializable {
    var originalPath: String?
    var compressPath: String? = null
    var fromType: FromType?
    var cropped: Boolean? = null
    var compressed: Boolean? = null

    private constructor(path: String, fromType: FromType) {
        this.originalPath = path
        this.fromType = fromType
    }

    companion object {
        fun of(uri: Uri, fromType: FromType): TImage {
            return TImage(uri.path, fromType)
        }

        fun of(path: String, fromType: FromType): TImage {
            return TImage(path, fromType)
        }
    }

    enum class FromType {
        CAMERA, OTHER
    }
}

