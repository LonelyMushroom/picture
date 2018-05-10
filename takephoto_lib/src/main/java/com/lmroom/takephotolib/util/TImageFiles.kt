package com.lmroom.takephoto.util

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.lmroom.takephotolib.R
import com.lmroom.takephotolib.model.TException
import com.lmroom.takephotolib.model.TExceptionType
import java.io.*
import java.util.*

object TImageFiles {

    /**
     * 将bitmap写入到文件
     */
    fun writeToFile(bitmap: Bitmap?, imageUri: Uri?) {
        if (bitmap == null || imageUri == null) return
        val file = File(imageUri.path)
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(bos.toByteArray())
            bos.flush()
            fos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) try {
                fos.close()
                bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * InputStream 转File
     */
    fun inputStreamToFile(inputStream: InputStream?, file: File?) {
        if (file == null) {
            Log.i(TAG, "inputStreamToFile:file not be null")
            throw TException(TExceptionType.TYPE_WRITE_FAIL)
        }

        var fos: FileOutputStream? = null
        val buffer = ByteArray(1024 * 10)
        try {
            fos = FileOutputStream(file)
            do {
                val i = inputStream?.read(buffer) ?: break
                if (i == -1) break
                fos.write(buffer, 0, i)
            } while (true)
        } catch (e: IOException) {
            Log.e(TAG, "InputStream 写入文件出错:" + e.toString())
            throw TException(TExceptionType.TYPE_WRITE_FAIL)
        } finally {
            try {
                fos?.flush()
                fos?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getTempFile(activity: Activity, photoUri: Uri): File {
        val minType = getMimeType(activity, photoUri)
        if (!checkMimeType(activity, minType)) throw TException(TExceptionType.TYPE_NOT_IMAGE)
        val filesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!filesDir!!.exists()) filesDir.mkdirs()
        return File(filesDir, UUID.randomUUID().toString() + "." + minType)
    }

    /**
     * 检查文件类型是否是图片
     */
    fun checkMimeType(context: Context, minType: String?): Boolean {
        minType?.let {
            val isPicture = if (TextUtils.isEmpty(minType)) false else ".jpg|.gif|.png|.bmp|.jpeg|.webp|".contains(minType.toLowerCase())
            if (!isPicture) Toast.makeText(context, context.resources.getText(R.string.tip_type_not_image), Toast.LENGTH_SHORT).show()
            return isPicture
        }
        return false
    }

    fun getMimeType(context: Activity, uri: Uri): String? {
        var extension: String?
        //Check uri format to avoid null
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            //If scheme is a content
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
            if (TextUtils.isEmpty(extension)) extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            if (TextUtils.isEmpty(extension)) extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
        }
        if (TextUtils.isEmpty(extension)) {
            extension = getMimeTypeByFileName(TUriParse.getFileWithUri(uri, context)?.name ?: "")
        }
        return extension
    }

    fun getMimeTypeByFileName(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length)
    }
}