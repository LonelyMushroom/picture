package com.lmroom.takephotolib.compress

import com.lmroom.takephotolib.model.TImage

interface CompressImage {
    fun compress()

    /**
     * 压缩结果监听器
     */
    interface CompressListener {
        /**
         * 压缩成功
         * @param images 已经压缩图片
         */
        fun onCompressSuccess(images: ArrayList<TImage>)
        /**
         * 压缩失败
         * @param images 压缩失败的图片
         * @param msg 失败的原因
         */
        fun onCompressFailed(images: ArrayList<TImage>?, msg: String)
    }
}