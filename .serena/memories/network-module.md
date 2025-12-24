# 网络模块 (module_network)

## 核心文件
- `RetrofitFactory.kt` - 网络客户端工厂
- `HeaderInterceptor.kt` - 请求头拦截器
- `RetryInterceptor.kt` - 重试拦截器
- `ApiConfig.kt` - API 配置
- `ApiException.kt` - 异常定义
- `RequestState.kt` - 请求状态封装
- `BaseHttpResult.kt` - 响应基类

## 关键类
- `BaseHeaderInterceptor` - 请求头拦截器基类
- `NetworkModule` - Koin 网络模块
- `@BaseUrl` - URL 注解
- `@ApplicationInterceptors` - 应用拦截器注解
- `@NetworkInterceptors` - 网络拦截器注解

## 使用示例
```kotlin
// 继承 BaseHeaderInterceptor 添加自定义请求头
class HeaderInterceptor : BaseHeaderInterceptor() {
    override fun headerList(): Map<String, String> = mapOf(
        "version" to BuildConfig.VERSION_NAME,
        "User-Agent" to "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}"
    )
}
```
