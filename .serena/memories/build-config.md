# 构建与配置

## Convention Plugins (build-logic/convention/)
- `ykmagic.android.application` — Android Application + 通用依赖
- `ykmagic.android.library` — Android Library + 通用依赖
- `ykmagic.android.compose` — Compose BOM + Material3 + 工具
- `ykmagic.android.koin` — Koin BOM + Annotations + 编译时检查
- `ykmagic.android.room` — Room3 + KSP
- `ykmagic.android.wire` — Wire Protobuf

通用依赖自动添加：core-ktx, appcompat, lifecycle-runtime-ktx, coroutines, testBundle, androidTestBundle

## 构建命令
```bash
./android_build.sh dev / build / all / clean / install / dependency
./gradlew test / lint / signingReport
```

## 必需配置文件
- `android_env.json` — 应用配置 (base_url)
- `keystore.properties` — 签名配置

## Debug 工具 (仅 Debug 版本)
- Chucker 4.3.1 — 网络抓包
- LeakCanary 3.0-alpha-8 — 内存泄漏检测
- Glance 1.1.0 — 性能监控