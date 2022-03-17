package com.yk.ykconfig.annotations


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class YkConfigValue(val name: String)
