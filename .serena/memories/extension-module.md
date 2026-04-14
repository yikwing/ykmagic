# 扩展模块 (module_extension)

## 目录结构
```
module_extension/src/main/java/com/yikwing/extension/
├── activity/      # Activity 扩展
├── app/           # Application 扩展
├── collection/    # 集合扩展
├── coroutines/    # 协程扩展 (collectInLifecycle, throttleFirst)
├── date/          # 日期扩展
├── delegate/      # 委托扩展
├── fragment/      # Fragment 扩展
├── image/         # 图片压缩 (compressImageFromUri)
├── io/            # IO 扩展 (copyAssetToCache)
├── network/       # NetConnectManager (StateFlow 网络状态监听)
├── util/          # CacheManager (LRU+TTL) / SessionManager / InitState<T>
├── view/          # View 扩展
├── GlobalContextProvider.kt
└── ExtensionInitProvider.kt
```

## 关键组件
- `CacheManager` — 线程安全 LRU 缓存 (默认 256 条, TTL 过期)
- `NetConnectManager` — 响应式网络状态 (WIFI/CELLULAR/ETHERNET/VPN/NONE, Closeable)
- `SessionManager` — 会话时长记录 (kotlin.time API)
- `InitState<T>` — 初始化状态密封类 (Uninitialized/Value<T>)