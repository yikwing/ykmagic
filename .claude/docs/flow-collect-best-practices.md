# Flow 收集最佳实践

## 核心原则

收集 Flow 必须绑定 UI 生命周期，防止两类问题：
- **后台泄漏**：UI 不可见时仍在消费事件、更新已销毁的 View
- **重复订阅**：重建后老协程未停止，新协程再次叠加

`repeatOnLifecycle` 是官方推荐的解决方案，它在指定状态（STARTED/RESUMED）时启动，离开时取消，再次进入时重新启动。

---

## Activity

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchWhenStarted {
            viewModel.uiState.collect { render(it) }
        }
    }
}
```

`launchWhenStarted` / `launchWhenResumed` 定义在 `FlowOnLifecycle.kt`，内部使用
`lifecycleScope.launch { repeatOnLifecycle(...) }`。

生命周期由 Activity 自身管理，**无需手动 cancel**。

---

## Fragment

```kotlin
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchWhenStarted {
            viewModel.uiState.collect { render(it) }
        }
    }
}
```

**关键**：必须在 `onViewCreated` 之后调用，`viewLifecycleOwner` 在此之前不可用。
绑定的是 `viewLifecycleOwner`（View 生命周期），而非 Fragment 自身生命周期，
因此 Fragment detach 重新 attach 时不会产生重复订阅。

**不要**在 `onCreate` 或 `init` 中调用。

---

## 自定义 View

自定义 View 无法使用扩展函数（需要持有 Job 引用），需手动实现双层约束：

```kotlin
class MyCustomView @JvmOverloads constructor(...) : View(...) {

    private var collectJob: Job? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (collectJob != null) return          // 防止重复 attach

        val owner = findViewTreeLifecycleOwner() ?: return
        collectJob = owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // collect / observe
            }
        }
    }

    override fun onDetachedFromWindow() {
        collectJob?.cancel()
        collectJob = null
        super.onDetachedFromWindow()
    }
}
```

两层约束：
- `repeatOnLifecycle(STARTED)`：宿主不可见时暂停收集
- `cancel` on detach：View 脱离窗口时彻底停止，避免泄漏

`findViewTreeLifecycleOwner()` 需在 View attach 到有 LifecycleOwner 的窗口后才有值，
因此入口选 `onAttachedToWindow` 而非构造函数。

---

## 为什么不提供 View 扩展函数

`FlowOnLifecycle.kt` 曾有 `View.launchWhenStarted`，已删除。原因：

扩展函数是**无状态**的，无法持有 `collectJob`。调用方每次调用都会启动一个新协程，
若 View 多次 attach/detach 则协程叠加。只提供 `repeatOnLifecycle` 约束而缺少 cancel-on-detach，
会产生"一行搞定"的错觉，实际只做了一半。

---

## 选哪个状态？

| 状态 | 适用场景 |
|------|----------|
| `STARTED` | 绝大多数 UI 更新（推荐默认值） |
| `RESUMED` | 必须前台可交互才处理的事件（如相机预览、游戏帧） |