package com.lmroom.takephotolib.permission;


import com.lmroom.takephotolib.model.InvokeParam;

/**
 * 授权管理回调
 */
public interface InvokeListener {
    PermissionManager.TPermissionType invoke(InvokeParam invokeParam);
}
