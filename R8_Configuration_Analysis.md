# R8 Configuration Analysis

## R8 配置

- **AGP 版本**: 9.1.1 — 已启用 AGP 9 的 app optimization 默认优化
- **Release 构建** (`app/build.gradle.kts`): `isMinifyEnabled = true`, `isShrinkResources = true`,使用 `proguard-android-optimize.txt` + `proguard-rules.pro` ✅
- **Library 模块** (`module_config` / `module_permission` / `module_extension` / `module_network` / `module_compose` / `module_proxy`): `isMinifyEnabled = false` ✅(library 模块通常由 app 端统一混淆,符合规范)
- **混淆字典**: `app/proguard-rules.pro` 配置了 `obfuscationdictionary` / `classobfuscationdictionary` / `packageobfuscationdictionary`,使用 `proguard_keywords.txt` — 属于混淆增强,不涉及 keep 规则,保留
- **配置导出**: `-printconfiguration ./build/r8/full-r8-config.txt` — 便于审计最终合并后的规则,保留

所有 module 的 `consumer-rules.pro` 均为空,没有向下游传递任何 keep 规则,良好。

---

## Keep 规则评估

整个工程目前**只有 1 条实质性 keep 规则**,位于 `module_extension/proguard-rules.pro`:

### 规则 1: `@androidx.annotation.Keep <fields>` (module_extension/proguard-rules.pro)

```proguard
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
}
```

**动作: 建议移除。**

**理由**: 该规则被 AGP 默认的 `proguard-android-optimize.txt` 完全覆盖(subsumed)。默认文件中已包含:

```proguard
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
```

其中 `@androidx.annotation.Keep *;` 已经 keep 了被该注解标注的**所有成员**(字段、方法、构造器),范围严格大于当前规则的 `<fields>`。因此该规则冗余,直接删除即可,R8 行为不变。

> 附注: 该文件中的大段注释讨论了 `TrustExtensions` 属性委托在使用属性名作 Bundle key 时的混淆风险。真正稳妥的方案是**代码层使用显式 `key = "xxx"` 参数**(注释中方案 1)。在未显式指定 key 的属性上加 `@Keep`,再依赖 AGP 默认规则即可——不需要再额外写这条 proguard 规则。

---

## 总结

| 优先级 | 规则 | 动作 |
|--------|------|------|
| 清理冗余 | `module_extension`: `@Keep <fields>` | 移除(被 AGP 默认规则覆盖) |

清理后,整个工程将不含任何自定义 keep 规则,完全依赖 AGP 9 默认优化和库自带 consumer rules——非常健康的状态。

---

## 验证建议

移除上述规则后,建议使用 [UI Automator](https://developer.android.com/training/testing/other-components/ui-automator) 对以下场景做 release 构建回归:

- 使用 `intIntent()` / `stringArgument()` 等 `TrustExtensions` 属性委托的 Activity/Fragment 启动与参数读取
- 使用 `@Keep` 注解标注的字段(若有)在反射路径上的行为

重点关注 `module_extension` 下使用属性委托的调用方,确保参数传递正常。