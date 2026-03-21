# 模块架构

## 模块列表

| 模块 | 路径 | 功能 |
|------|------|------|
| app | app/ | 示例应用，演示框架使用 |
| module_config | module_config/ | 配置注入 (@YkConfigNode) |
| module_network | module_network/ | Ktor Client 网络请求封装 |
| module_extension | module_extension/ | Kotlin 扩展函数集合 |
| module_datastore | module_datastore/ | Proto DataStore 封装 |
| module_permission | module_permission/ | 运行时权限请求 |
| module_proxy | module_proxy/ | BaseActivity、AppInitializer |
| module_component | module_component/ | 自定义 View 组件 |

注：module_logger 已移除（7617cf7），日志功能由 android.util.Log 直接使用。

## 模块依赖关系
```
app
 ├── module_config
 ├── module_network
 ├── module_extension
 ├── module_datastore
 ├── module_permission
 ├── module_proxy
 └── module_component
```
