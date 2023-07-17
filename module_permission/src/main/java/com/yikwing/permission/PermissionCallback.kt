package com.yikwing.permission

/**
 *
 * @return Boolean  是否全部授权
 *
 * @return List 未授权的权限列表
 * */
typealias PermissionCallback = (Boolean, List<String>) -> Unit
