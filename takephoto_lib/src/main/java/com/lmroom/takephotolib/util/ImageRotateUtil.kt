package com.lmroom.takephoto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageRotateUtil {

    /**
     * 纠正照片的旋转角度
     */
    fun correctImage(context: Context, path: Uri) {
        val imagePath = TUriParse.parseOwnUri(context, path)
        val degree: Int = getBitmapDegree(imagePath)
        if (degree != 0) {
            val bitmap = BitmapFactory.decodeFile(imagePath) ?: return
            val resultBitMap = rotateBitmapByDegree(bitmap, degree) ?: return
            try {
                resultBitMap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(File(imagePath)))
            } catch (e: Exception) {
            }
        }
    }


    /**
     * 读取图片的旋转的角度
     */
    fun getBitmapDegree(path: String?): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }


    /**
     * 将图片按照某个角度进行旋转
     */
    fun rotateBitmapByDegree(bitmap: Bitmap?, degree: Int?): Bitmap? {
        var returnBitmap: Bitmap? = null

        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree?.toFloat() ?: 0F)

        try {
            bitmap?.let {
                returnBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.width, bitmap.height,
                        matrix, true)
            }
        } catch (e: OutOfMemoryError) {
        }
        if (returnBitmap == null) {
            returnBitmap = bitmap
        }
        bitmap?.recycle()
        return returnBitmap
    }
}