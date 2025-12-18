package com.yikwing.ykquickdev.components

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode

/**
 * 空 Indication，用于全局取消点击效果（ripple）
 */
object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = object : Modifier.Node(), DelegatableNode {}

    override fun hashCode(): Int = -1

    override fun equals(other: Any?): Boolean = other === this
}
