package com.yikwing.extension.device

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * 折叠屏姿态判定说明
 * ---
 * Jetpack WindowManager 的 FoldingFeature.State 只有两种：
 *   - FLAT        完全展开（180°）
 *   - HALF_OPENED 半折叠（约 90°）
 *
 * 没有 "CLOSED" 状态。设备合上时内屏熄灭，Activity 进入 onStop()，
 * foldingFeatureFlow 自然停止发射，因此无法通过 WindowManager 判定"合上"。
 * 业务上直接按"普通屏幕"处理即可——合上若跑在外屏，外屏即普通手机屏。
 *
 * 姿态对照表：
 *   普通屏幕      → hasHingeSensor() == false 且 feature 始终为 null
 *   折叠屏展开    → state == FLAT（feature != null）
 *   折叠屏书本    → state == HALF_OPENED && orientation == VERTICAL
 *   折叠屏桌面    → state == HALF_OPENED && orientation == HORIZONTAL
 *   折叠屏合上/外屏 → feature == null（Activity RESUMED，窗口为外屏尺寸，视同普通屏幕）
 *   折叠屏完全合上 → Activity onStop() 回调；如需精确，订阅 TYPE_HINGE_ANGLE 传感器
 *
 * 若设备支持外屏续航（如 Galaxy Fold / Pixel Fold），合上后会收到
 * onConfigurationChanged 切到外屏尺寸，可作为辅助信号。
 */

/**
 * 静态判断：设备是否上报铰链角度传感器（需 API 30+）。
 * 并非所有折叠屏都会暴露该特性（如部分翻盖机），仅作 hint 使用，
 * 精确判定应订阅 [foldingFeatureFlow]。
 */
fun Context.hasHingeSensor(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)

/**
 * 实时监听折叠姿态。当前无铰链特性（平展设备或外接显示）时发射 `null`。
 *
 * 需在主线程以 `repeatOnLifecycle(STARTED)` 收集，避免后台态持续订阅。
 *
 * 注意：完全合上状态下 Activity 不可见，此 Flow 不会发射任何值——
 * 详见本文件顶部的"折叠屏姿态判定说明"。
 */
fun Activity.foldingFeatureFlow(): Flow<FoldingFeature?> =
    WindowInfoTracker
        .getOrCreate(this)
        .windowLayoutInfo(this)
        .map { info -> info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull() }

/** 当前处于半折叠（书本/桌面模式）。 */
val FoldingFeature?.isHalfOpened: Boolean
    get() = this?.state == FoldingFeature.State.HALF_OPENED

/** 铰链将逻辑显示区域分割为两部分，布局需做双屏适配。 */
val FoldingFeature?.isSeparating: Boolean
    get() = this != null && this.isSeparating

/** 书本模式：竖向铰链 + 半折叠（左右分屏）。 */
val FoldingFeature?.isBookPosture: Boolean
    get() = this?.state == FoldingFeature.State.HALF_OPENED && this.orientation == FoldingFeature.Orientation.VERTICAL

/** 桌面/帐篷模式：横向铰链 + 半折叠（上下分屏）。 */
val FoldingFeature?.isTableTopPosture: Boolean
    get() = this?.state == FoldingFeature.State.HALF_OPENED && this.orientation == FoldingFeature.Orientation.HORIZONTAL

/**
 * 折叠屏姿态枚举，配合 [posture] 使用。
 *
 * - [NORMAL]   普通屏幕；等价场景：折叠屏合上后跑在外屏（外屏 = 普通手机屏）
 * - [OPENED]   折叠屏完全展开（平展大屏）
 * - [HALF]     折叠屏半折叠（书本 / 桌面 / 帐篷）
 *
 * 不单独区分"合上"——合上时 Activity 要么 STOP（内屏），要么切到外屏（等同 NORMAL）。
 */
enum class FoldPosture { NORMAL, OPENED, HALF }

/**
 * 根据当前 [FoldingFeature] 判定姿态。
 *
 * feature == null 时（合上外屏 / 普通屏）一律视为 NORMAL；
 * 只有 state == FLAT 时才认定为展开。
 */
fun FoldingFeature?.posture(): FoldPosture =
    when (this?.state) {
        FoldingFeature.State.HALF_OPENED -> FoldPosture.HALF
        FoldingFeature.State.FLAT -> FoldPosture.OPENED
        else -> FoldPosture.NORMAL
    }
