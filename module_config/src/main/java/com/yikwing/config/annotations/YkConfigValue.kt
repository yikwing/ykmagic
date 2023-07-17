package com.yikwing.config.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class YkConfigValue(val path: String)
