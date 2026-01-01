package com.yikwing.extension.fragment

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * DialogFragment 显示结果
 */
sealed class DialogShowResult {
    /** 成功显示 */
    data object Success : DialogShowResult()

    /** 显示失败 */
    sealed class Failure : DialogShowResult() {
        /** tag 重复，Dialog 已在显示中 */
        data object AlreadyShowing : Failure()

        /** 生命周期异常：状态已保存 */
        data object StateSaved : Failure()

        /** 生命周期异常：FragmentManager 已销毁 */
        data object Destroyed : Failure()
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

/**
 * 安全显示 DialogFragment，返回详细结果用于区分失败原因
 *
 * @param dialog 要显示的 DialogFragment
 * @param tag Fragment 标识，用于查找和管理
 * @return [DialogShowResult] 显示结果
 */
fun FragmentManager.showIfNotShowingWithResult(
    dialog: DialogFragment,
    tag: String,
): DialogShowResult {
    if (isDestroyed) return DialogShowResult.Failure.Destroyed
    if (isStateSaved) return DialogShowResult.Failure.StateSaved
    if (findFragmentByTag(tag) != null) return DialogShowResult.Failure.AlreadyShowing
    dialog.show(this, tag)
    return DialogShowResult.Success
}

/**
 * 安全显示 DialogFragment，使用类名作为默认 tag
 */
fun FragmentManager.showIfNotShowingWithResult(dialog: DialogFragment): DialogShowResult =
    showIfNotShowingWithResult(dialog, dialog::class.java.simpleName)

/**
 * 安全显示 DialogFragment（简化版，仅返回是否成功）
 */
fun FragmentManager.showIfNotShowing(
    dialog: DialogFragment,
    tag: String,
): Boolean = showIfNotShowingWithResult(dialog, tag).isSuccess

/**
 * 安全显示 DialogFragment，使用类名作为默认 tag（简化版）
 */
fun FragmentManager.showIfNotShowing(dialog: DialogFragment): Boolean = showIfNotShowing(dialog, dialog::class.java.simpleName)
