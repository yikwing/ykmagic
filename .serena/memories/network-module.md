# 网络模块 (module_network)

## 核心文件
- `NetworkModule.kt` — Koin 模块 + @ComponentScan + Json 配置
- `ApiTransform.kt` — requestStateFlow() / requestResult() 请求转换
- `RequestState.kt` — 请求状态封装 (Loading/Success/Error) + DSL (collectState)
- `BaseHttpResult.kt` — HTTP 响应基类 (data/errorMsg/errorCode)
- `ApiConfig.kt` — 全局错误码策略 (errorCodeChecker)
- `ApiException.kt` — 异常定义
- `NetworkUtil.kt` — 网络工具
- `NetworkQualifiers.kt` — Koin 限定符 (@BaseUrl, @DebugFlag)

## 架构
- NetworkModule 提供 Json 配置，通过 @ComponentScan 扫描网络层
- HttpClient 由 app 层 AppNetworkModule 构建 (Ktor CIO + HttpTimeout/ContentNegotiation/DefaultRequest/HttpRequestRetry/Logging)
- app 的 AppModule includes NetworkModule