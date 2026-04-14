# 模块架构

## 模块列表

| 模块 | 功能 |
|------|------|
| app | 示例应用，演示框架使用 |
| module_config | 配置注入 (YkConfigManager + kotlinx.serialization) |
| module_network | Ktor Client 网络封装 (RequestState/ApiTransform/ApiException) |
| module_extension | Kotlin 扩展函数 (CacheManager/NetConnectManager/SessionManager/InitState) |
| module_compose | 通用 Compose 组件 (Loading/Error/Image/Center/Debounce/SystemBars/AppLifecycleObserver) |
| module_permission | 运行时权限请求 (Fragment 封装) |
| module_proxy | BaseActivity/BaseFragment/AppInitializer/ActivityHierarchyManager |

已移除模块：module_datastore、module_component、module_logger。DataStore 在 app 模块内管理。

## 依赖关系
```
app → module_config / module_network / module_extension / module_compose / module_permission / module_proxy
```