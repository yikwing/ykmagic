# 构建与配置

## 构建命令
```bash
./android_build.sh dev      # Debug 构建
./android_build.sh build    # Release 构建
./android_build.sh all      # 清理+构建+安装
./gradlew test              # 运行测试
```

## 必需配置文件
- `android_env.json` - 应用配置 (base_url 等)
- `keystore.properties` - 签名配置

## Debug 工具
- Chucker 4.2.0 - 网络抓包
- LeakCanary 3.0-alpha-8 - 内存泄漏检测
- Glance 1.1.0 - 性能监控
