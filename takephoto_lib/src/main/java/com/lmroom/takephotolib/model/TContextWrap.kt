package com.lmroom.takephotolib.model

import android.app.Activity
import android.support.v4.app.Fragment

class TContextWrap {
    var activity: Activity
    var fragment: Fragment? = null

    constructor(activity: Activity) {
        this.activity = activity
    }

    constructor(fragment: Fragment) {
        this.activity = fragment.activity!!
        this.fragment = fragment
    }

}
