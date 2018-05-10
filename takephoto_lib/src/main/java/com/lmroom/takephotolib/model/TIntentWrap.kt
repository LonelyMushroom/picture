package com.lmroom.takephotolib.model

import android.content.Intent

data class TIntentWrap(
        val intent: Intent,
        val requestCode: Int
)