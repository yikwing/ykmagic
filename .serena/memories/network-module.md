# 网络模块 (module_network)

## 核心文件
- `NetworkModule.kt` - Koin 模块 + @ComponentScan + JSON 配置
- `ApiTransform.kt` - 请求转换（requestStateFlow / requestResult）
- `RequestState.kt` - 请求状态封装（Loading/Success/Error）
- `BaseHttpResult.kt` - HTTP 响应基类
- `HeaderInterceptor.kt` - 请求头拦截器
- `RetryInterceptor.kt` - 重试拦截器
- `ApiConfig.kt` - API 配置
- `ApiException.kt` - 异常定义
- `NetworkUtil.kt` - 网络工具
- `NetworkQualifiers.kt` - Koin 限定符
- `log/RequestTracker.kt` - HTTP 日志追踪（含内联的 LogEntry 和格式化逻辑）

## DI 配置
- `NetworkModule` 同时承担 Koin 模块定义和 `@ComponentScan("com.yikwing.network")`
- 已移除独立的 `NetworkScanModule`（合并到 NetworkModule）
- app 的 `AppModule` 直接引用 `NetworkModule`