# Android Studio 配置技巧

## 字体连字 (Font Ligatures)

启用 OpenType 字体特性，让代码更美观易读。

### 配置方法

1. `Help` → `Edit Custom VM Options...`
2. 添加以下配置：

```
-Deditor.font.features=calt,cv01,ss01,zero
```

3. 重启 Android Studio

### 特性说明

| 特性 | 作用 | 示例 |
|------|------|------|
| `calt` | 上下文替代 | 自动调整字符间距 |
| `liga` | 标准连字 | `!=` → `≠`, `->` → `→` |
| `cv01` | 字符变体 1 | 字体特定样式 |
| `ss01` | 风格集 1 | 字体特定样式 |
| `zero` | 斜线零 | `0` → `∅` |

### 推荐字体

| 字体 | 官网 |
|------|------|
| JetBrains Mono | https://www.jetbrains.com/lp/mono/ |
| Monaspace | https://monaspace.githubnext.com/ |
| Maple Mono | https://font.subf.dev/zh-cn/ |
| Fira Code | https://github.com/tonsky/FiraCode |
| Cascadia Code | https://github.com/microsoft/cascadia-code |