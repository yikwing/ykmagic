# `@IntDef` 与位标志模式

两个小技巧在 Kotlin/Android 里常用，原出处是 `module_proxy/src/test/` 下的
`StringDefDemo.kt` 与 `StatusModel.kt`（已删除，迁至本文档留档）。

## 1. `@IntDef` — 轻量的类型安全常量

用 `@IntDef` 约束 `Int` 参数必须是一组已知常量之一，比 `enum class` 更省开销，
比裸 `Int` 更安全。

```kotlin
import androidx.annotation.IntDef

@IntDef(SEX.MAN, SEX.WOMAN, SEX.PRIVACY)
@Retention(AnnotationRetention.SOURCE)
annotation class SEX {
    companion object {
        const val MAN = 0x0001
        const val WOMAN = 1 shl 1
        const val PRIVACY = 1 shl 2
    }
}

fun getSexChineseStr(@SEX sex: Int): String =
    when (sex) {
        SEX.MAN -> "男"
        SEX.WOMAN -> "女"
        SEX.PRIVACY -> "隐私"
        else -> throw IllegalArgumentException("Unknown sex=$sex")
    }
```

**要点**
- `@Retention(SOURCE)`：只在编译期做 lint 检查，不留 runtime 元数据，零开销。
- 常量写在 `companion object` 里并用 `const val`，调用方按 `SEX.MAN` 访问。
- 传非法值不会抛编译错，只会触发 Android Lint 的 `WrongConstant`；因此 `when` 的
  `else` 分支仍要兜底。
- 若常量需要做位组合（`SEX.MAN or SEX.PRIVACY`），给注解加 `flag = true`。

## 2. 位标志（bit flags）

用一组互不重叠的 2^n 常量描述多状态集合，优点是"存一个 Int、读写都是 O(1)"。

```kotlin
const val snow  = 0x0001   // 1 shl 0
const val rain  = 0x0002   // 1 shl 1
const val sun   = 0x0004   // 1 shl 2
const val cloud = 0x0008   // 1 shl 3
// 超过 Int 的 32 位就改用 Long

// 组合：雪 + 雨
const val WEATHER_A = snow or rain

// 从 WEATHER_A 去掉 rain，再加上 sun => 雪 + 晴
const val WEATHER_B = WEATHER_A and rain.inv() or sun
```

**三种基础操作**

| 操作 | 写法 | 含义 |
|---|---|---|
| 加入状态 | `flags or STATUS` | 把位打开（幂等） |
| 移除状态 | `flags and STATUS.inv()` | 把位关掉 |
| 判断状态 | `(flags and STATUS) != 0` | 非零表示包含 |

```kotlin
val hasRain = (WEATHER_A and rain) != 0  // true
val hasSun  = (WEATHER_B and sun) != 0   // true
```

**配合 `@IntDef(flag = true)`** 可以让编译器/Lint 同时校验位组合的合法性：

```kotlin
@IntDef(flag = true, value = [snow, rain, sun, cloud])
@Retention(AnnotationRetention.SOURCE)
annotation class Weather
```

**何时用**
- 状态取值稀疏、互不冲突，且需要组合表达（如权限、UI 模式标志位）。
- 状态之间互斥、数量有限，优先用 `enum class` / `sealed class`。
