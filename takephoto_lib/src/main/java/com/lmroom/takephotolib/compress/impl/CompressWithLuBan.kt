package com.lmroom.takephotolib.compress.impl

import android.content.Context
import android.util.Log
import com.lmroom.takephotolib.compress.CompressImage
import com.lmroom.takephotolib.model.TImage
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

/**
 * 压缩配置类
 */
class CompressWithLuBan : CompressImage {

    @Volatile
    private var imagesCount: Int = 0
    val images: ArrayList<TImage>?
    val listener: CompressImage.CompressListener?
    private val context: Context?
    private val files: ArrayList<String> = ArrayList()

    constructor(context: Context, images: ArrayList<TImage>, listener: CompressImage.CompressListener) {
        this.context = context
        this.images = images
        this.listener = listener
        this.imagesCount = images.size
    }

    /**
     * 压缩单个
     */
    private fun compressOne() {
        doAsync {
            Luban.with(context)
                    .load(files[0])
                    .setCompressListener(object : OnCompressListener {
                        override fun onSuccess(file: File?) {
                            Log.d("luban", "压缩成功")
                            uiThread {
                                images?.get(0)?.let {
                                    it.compressPath = file?.path
                                    it.compressed = true
                                    listener?.onCompressSuccess(images)
                                }
                            }
                        }

                        override fun onError(e: Throwable?) {
                            Log.d("luban", "压缩失败")
                            uiThread {
                                listener?.onCompressFailed(images, e?.message ?: "")
                            }
                        }

                        override fun onStart() {
                        }
                    })
                    .launch()
        }
    }

    /**
     * 压缩多个
     */
    private fun compressMulti() {
        doAsync {
            files.mapIndexed { index, string ->
                Luban.with(context)
                        .load(string)
                        .setCompressListener(object : OnCompressListener {
                            override fun onSuccess(file: File?) {
                                Log.d("luban", "压缩成功")
                                images?.let {
                                    it[index].compressPath = file?.path
                                    it[index].compressed = true
                                }
                                imagesCount--
                                if (imagesCount == 0) {
                                    uiThread {
                                        listener?.onCompressSuccess(images ?: ArrayList())
                                    }
                                }
                            }

                            override fun onError(e: Throwable?) {
                                Log.d("luban", "压缩失败")
                                uiThread {
                                    listener?.onCompressFailed(images, e?.message ?: "")
                                }
                            }

                            override fun onStart() {

                            }
                        })
                        .launch()
            }
        }
    }

    override fun compress() {
        if (images == null || images.isEmpty()) {
            listener?.onCompressFailed(images, "images is null")
            return
        }
        images.forEach {
            files.add(it.originalPath ?: "")
        }
        if (images.size == 1) {
            compressOne()
        } else {
            compressMulti()
        }
    }

}