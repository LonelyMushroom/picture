package com.lmroom.takephoto.util

import android.content.Context
import android.util.Log
import java.io.File

object TFileUtils {
    private val TAG = "TFileUtils"
    private val DEFAULT_DISK_CACHE_DIR = "takephoto_cache"

    fun getPhotoCacheDir(context: Context?, file: File?): File? {
        context?.cacheDir?.let {
            val file1 = File(it, DEFAULT_DISK_CACHE_DIR)
            return if (!file1.mkdirs()
                    && (!file1.exists() || !file1.isDirectory)) {
                file1
            } else {
                File(file1, file?.name)
            }
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null")
        }
        return file
    }

    fun delete(path: String?) {
        try {
            path ?: return
            val file = File(path)
            if (!file.delete()) {
                file.deleteOnExit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}