# Android 开发

## 主要用到的技术

- 使用 gradle 进行项目构建
- 使用 kotlin 搭建整个系统
- 使用 配置文件 按需注入
- 使用 ksp 等

## 目前分为六个模块

- **module_config**：inject json 配置数据, 维护 application context
- **module_extension**：各种扩展方法/工具类
- **module_logger**：统一日志组件/基于 logger
- **module_network**：封装统一 network 提供注入拦截器/debug 模式增加网络抓包视图
- **module_permission**：基于 fragment 封装统一权限请求
- **module_proxy**：统一 base 组件

## 问题：怎么依赖 module？

1. 添加 jitpack 仓库地址

   ```gradle
    maven {
        url = uri("https://jitpack.io")
    }
   ```

2. 按需引入依赖 (以最新版本为主)

   ```gradle
   implementation("com.github.yikwing.ykmagic:config:0.1.0")
   implementation("com.github.yikwing.ykmagic:network:0.1.0")
   implementation("com.github.yikwing.ykmagic:proxy:0.1.0")
   implementation("com.github.yikwing.ykmagic:extension:0.1.0")
   implementation("com.github.yikwing.ykmagic:permission:0.1.0")
   implementation("com.github.yikwing.ykmagic:logger:0.1.0")
   ```

## Tips

- 使用空 node package.json `scripts` 配置快速命令行命令
- sh 文件夹配置了 **签名** / **校验签名** 脚本

## TODO

- [x] 统一公共依赖
- [ ] 抽取 commom 模块,其他模块依赖基础模块
- [x] 统一第三方依赖版本
