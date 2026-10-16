package com.yikwing.compose.lifecycle

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class DropUnlessResumedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val lifecycleOwner =
        object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this)
        }

    @Test
    fun shouldInvokeWithArgumentOnlyWhenResumed() {
        val received = mutableListOf<String>()
        lateinit var onClick: (String) -> Unit

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                onClick = dropUnlessResumed { id: String -> received += id }
            }
        }

        composeRule.runOnIdle {
            lifecycleOwner.lifecycle.currentState = Lifecycle.State.STARTED
            onClick("started")
            lifecycleOwner.lifecycle.currentState = Lifecycle.State.RESUMED
            onClick("resumed")
            lifecycleOwner.lifecycle.currentState = Lifecycle.State.STARTED
            onClick("paused")
        }

        assertThat(received, `is`(listOf("resumed")))
    }
}
