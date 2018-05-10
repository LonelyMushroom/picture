package com.lmroom.takephotolib.model

import java.lang.reflect.Method

class InvokeParam{
    var proxy: Any
    var method: Method
    var args: Array<Any>

    constructor(proxy:Any,method: Method,args:Array<Any>){
        this.proxy = proxy
        this.method = method
        this.args = args
    }
}