package com.lmroom.takephoto.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.lmroom.takephotolib.R
import com.lmroom.takephotolib.model.*

object TUtils {

    /**
     * 将Uri集合转换成TImage集合
     */
    fun getTImagesWithUris(uris: ArrayList<Uri>, fromType: TImage.FromType): ArrayList<TImage> {
        val tImage = arrayListOf<TImage>()
        uris.forEach {
            tImage.add(TImage.Companion.of(it, fromType))
        }
        return tImage
    }

    fun startActivityForResult(contextWrap: TContextWrap, intentWrap: TIntentWrap) {
        if (contextWrap.fragment != null) {
            contextWrap.fragment?.startActivityForResult(intentWrap.intent, intentWrap.requestCode)
        } else {
            contextWrap.activity?.startActivityForResult(intentWrap.intent, intentWrap.requestCode)
        }
    }

    /**
     * 安全地发送Intent
     *
     * @param contextWrap
     * @param intentWapList 要发送的Intent以及候选Intent
     * @param defaultIndex  默认发送的Intent
     * @param isCrop        是否为裁切照片的Intent
     */
    fun sendIntentBySafely(contextWrap: TContextWrap, intentWraps: ArrayList<TIntentWrap>, defaultIndex: Int) {
        val tIntentWrap = intentWraps[defaultIndex]
        val result = contextWrap.activity?.packageManager?.queryIntentActivities(tIntentWrap.intent, PackageManager.MATCH_ALL)
        if (result?.isEmpty() == true) {
            sendIntentBySafely(contextWrap, intentWraps, defaultIndex.inc())
        } else {
            startActivityForResult(contextWrap, tIntentWrap)
        }
    }

    /**
     * 拍照前检查是否有相机
     */
    fun captureBySafely(contextWrap: TContextWrap, intentWap: TIntentWrap) {
        val result = contextWrap.activity?.packageManager?.queryIntentActivities(intentWap.intent, PackageManager.MATCH_ALL)
        if (result?.isEmpty() == true) {
            Toast.makeText(contextWrap.activity, contextWrap.activity?.resources?.getText(R.string.tip_no_camera), Toast.LENGTH_SHORT).show()
            throw TException(TExceptionType.TYPE_NO_CAMERA)
        } else {
            startActivityForResult(contextWrap, intentWap)
        }
    }

    /**
     * 显示圆形进度对话框
     * @param activity
     * @param progressTitle 显示的标题
     * @return
     * @author JPH
     * Date 2014-12-12 下午7:04:09
     */
    fun showProgressDialog(activity: Activity?,
                           vararg progressTitle: String): ProgressDialog? {
        if (activity == null || activity.isFinishing) return null
        var title = activity.resources.getString(R.string.tip_tips)
        if (progressTitle != null && progressTitle.size > 0)
            title = progressTitle[0]
        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle(title)
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

}