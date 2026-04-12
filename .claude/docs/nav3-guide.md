# Navigation 3 指南

## 三层职责分离

| 层 | 命名规则 | 后缀 | 职责 |
|---|---|---|---|
| Route | `XxxRoute` | `Route` | 路由定义（NavKey），描述"去哪" |
| Entry | `xxxEntry()` | `Entry` | 注册路由，连接 Route → Screen |
| Screen | `XxxScreen` | `Screen` | 纯 UI Composable，不关心导航 |

### 文件组织

- 文件名跟随主体 Composable：`XxxScreen.kt`
- Route、Entry、Screen 放在同一个文件中
- Route 定义在文件顶部（`@Serializable` 注解之后）

### 示例

```kotlin
// TextDebounceScreen.kt

@Serializable
data object TextDebounceRoute : NavKey          // Route: 路由定义

fun EntryProviderScope<NavKey>.textDebounceEntry() {  // Entry: 注册路由
    entry<TextDebounceRoute> {
        val navigator = LocalNavigator.current
        TextDebounceScreen(                     // Screen: 纯 UI
            navigationToPackInfo = dropUnlessResumed { navigator.navigate(PackageInfoRoute) },
        )
    }
}

@Composable
fun TextDebounceScreen(                         // Screen: 不感知导航细节
    navigationToPackInfo: () -> Unit,
) { /* UI */ }
```

### 带参数的 Route

```kotlin
@Serializable
data class ProductRoute(val id: String) : NavKey

fun EntryProviderScope<NavKey>.otherPageEntry() {
    entry<ProductRoute> { product ->
        OtherPageScreen(product.id)
    }
}
```

### 命名对照表

| Route (NavKey) | Entry 函数 | Screen (Composable) | 文件名 |
|---|---|---|---|
| `MainRoute` | `mainScreenEntry()` | `MainScreen()` | `MainScreen.kt` |
| `TextDebounceRoute` | `textDebounceEntry()` | `TextDebounceScreen()` | `TextDebounceScreen.kt` |
| `ProductRoute` | `otherPageEntry()` | `OtherPageScreen()` | `OtherPageScreen.kt` |
| `AuthLoginRoute` | `authLoginEntry()` | `AuthLoginScreen()` | `AuthLoginScreen.kt` |
| `AuthRegisterRoute` | `authRegisterEntry()` | `AuthRegisterScreen()` | `AuthRegisterScreen.kt` |
| `DiyInputRoute` | `diyInputEntry()` | `DiyInputScreen()` | `DiyInputScreen.kt` |
| `PackageInfoRoute` | `packageInfoEntry()` | `PackageInfoScreen()` | `PackageInfoScreen.kt` |

---

## Entry 生命周期行为

### Entry 与 Composition 的关系（重要！）

Nav3 导航时 entry **不是**保持在 STARTED，而是**被移出 Composition**，返回时重新进入。

```
操作              Composition          ViewModel/SavedState
─────────────────────────────────────────────────────────
进入 A            A 进入               A VM 创建
A → B（动画中）   A、B 同时存在        —
A → B（动画后）   A 移出，B 保留       A VM 存活（在 backStack）
B → 返回（动画中）A、B 同时存在        —
B → 返回（动画后）B 移出，A 重新进入   B VM 销毁
setRoot(C)        A 移出，C 进入       A VM 销毁
```

### 两个 Decorator 的分工

| Decorator | 保存内容 | 移除时机 |
|---|---|---|
| `rememberViewModelStoreNavEntryDecorator` | ViewModel 实例 | entry 从 backStack pop |
| `rememberSaveableStateHolderNavEntryDecorator` | `rememberSaveable` 值 | entry 从 backStack pop |

### 生命周期 Hook

由于 entry 离开时会被移出 Composition，**用 `DisposableEffect` 即可**，无需 `LifecycleResumeEffect`：

```kotlin
// 进入/返回时执行，离开时清理
DisposableEffect(Unit) {
    val listener = register()
    onDispose { listener.unregister() }  // 移出 Composition 后（动画结束后）触发
}
```

### WebView 在 Nav3 中的处理

由于 entry 移出 Composition 后 WebView 实例销毁，需通过 ViewModel 保存状态：

```kotlin
class WebViewModel : ViewModel() {
    val savedState = Bundle()  // 跟随 entry 存活，不随 Composition 销毁
}

AndroidView(
    factory = { context ->
        WebView(context).apply {
            if (!vm.savedState.isEmpty) restoreState(vm.savedState) else loadUrl(url)
        }
    },
    onRelease = { webView -> webView.saveState(vm.savedState) },
)
```
