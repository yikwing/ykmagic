package com.yikwing.extension.util

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * App 级会话管理器，记录会话的开始时间并计算已用时长。
 *
 * 以 [Instant.DISTANT_PAST] 作为"未开始"标记，避免引入额外的可空类型。
 *
 * 作为 App 全局单例使用：整个进程生命周期内只有一个会话时钟。如需多会话（例如
 * 按业务流程计时），请自行构建独立的计时器，不要复用此对象。
 */
object SessionManager {
    // DISTANT_PAST 表示会话未启动；调用 startSession() 后替换为实际时间戳
    private var sessionStart: Instant = Instant.DISTANT_PAST

    /** 会话是否已启动（即 [startSession] 已被调用且尚未 [resetSession]）。 */
    val isActive: Boolean get() = sessionStart != Instant.DISTANT_PAST

    /** 记录当前时刻为会话起点；若会话已在运行，则以新时刻覆盖。 */
    fun startSession() {
        sessionStart = Clock.System.now()
    }

    /** 返回自会话启动至今的时长；会话未启动时返回 [Duration.ZERO]。 */
    fun getSessionDuration(): Duration {
        if (!isActive) return Duration.ZERO
        return Clock.System.now() - sessionStart
    }

    /** 重置会话，使 [isActive] 变为 false。 */
    fun resetSession() {
        sessionStart = Instant.DISTANT_PAST
    }
}
