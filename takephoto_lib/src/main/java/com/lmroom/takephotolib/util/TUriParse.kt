package com.lmroom.takephoto.util

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import com.lmroom.takephotolib.model.TException
import com.lmroom.takephotolib.model.TExceptionType
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

object TUriParse {

    /**
     * 将scheme为file的uri转成FileProvider 提供的content uri
     */
    fun convertFileUriToFileProviderUri(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null
        return if (ContentResolver.SCHEME_FILE == uri.scheme)
            getUriForFile(context, File(uri.path))
        else uri
    }

    /**
     * 获取一个临时的Uri, 文件名随机生成
     */
    fun getTempUri(context: Context): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(Environment.getExternalStorageDirectory(), "/images/$timeStamp.jpg")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        return getUriForFile(context, file)
    }

    /**
     * 获取一个临时的Uri, 通过传入字符串路径
     */
    fun getTempUri(context: Context, path: String): Uri? {
        val file = File(path)
        return getTempUri(context, file)
    }

    /**
     * 获取一个临时的Uri, 通过传入File对象
     */
    fun getTempUri(context: Context, file: File): Uri? {
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        return getUriForFile(context, file)
    }

    /**
     * 创建一个用于拍照图片输出路径的Uri (FileProvider)
     */
    fun getUriForFile(context: Context, file: File): Uri? {
        return FileProvider.getUriForFile(context, TConstant.getFileProviderName(context), file)
    }

    /**
     * 将TakePhoto 提供的Uri 解析出文件绝对路径
     */
    fun parseOwnUri(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        return if (TextUtils.equals(uri.authority, TConstant.getFileProviderName(context))) {
            File(uri.path.replace("camera_photos/", "")).absolutePath
        } else {
            uri.path
        }
    }

    /**
     * 通过URI获取文件的路径
     */
    fun getFilePathWithUri(uri: Uri?, activity: Activity): String {
        if (uri == null) {
            Log.w(TAG, "uri is null,activity may have been recovered?")
            throw TException(TExceptionType.TYPE_URI_NULL)
        }
        val picture = getFileWithUri(uri, activity)
        val picturePath = picture?.path
        if (TextUtils.isEmpty(picturePath)) throw TException(TExceptionType.TYPE_URI_PARSE_FAIL)
        if (!TImageFiles.checkMimeType(activity, TImageFiles.getMimeType(activity, uri))) throw TException(TExceptionType.TYPE_NOT_IMAGE)
        return picturePath!!
    }

    /**
     * 通过URI获取文件
     */
    fun getFileWithUri(uri: Uri?, activity: Activity): File? {
        var picturePath: String? = null
        val scheme = uri?.scheme
        if (ContentResolver.SCHEME_CONTENT == scheme) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = activity.contentResolver.query(uri, filePathColumn, null, null, null)
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            if (columnIndex >= 0) {
                picturePath = cursor.getString(columnIndex)
            } else {
                picturePath = parseOwnUri(activity, uri)
            }
            cursor.close()
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            picturePath = uri.path
        }
        return if (TextUtils.isEmpty(picturePath)) null else File(picturePath)
    }

    /**
     * 通过从文件中得到的URI获取文件的路径
     */
    fun getFilePathWithDocumentsUri(uri: Uri?, activity: Activity): String? {
        if (uri == null) {
            Log.e(TAG, "uri is null,activity may have been recovered?")
            return null
        }
        if (ContentResolver.SCHEME_CONTENT == uri.scheme
                && uri.path.contains("document")) {
            val tempFile = TImageFiles.getTempFile(activity, uri)
            try {
                TImageFiles.inputStreamToFile(activity.contentResolver.openInputStream(uri), tempFile)
                return tempFile.path
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                throw TException(TExceptionType.TYPE_NO_FIND)
            }
        } else {
            return getFilePathWithUri(uri, activity)
        }
    }
}