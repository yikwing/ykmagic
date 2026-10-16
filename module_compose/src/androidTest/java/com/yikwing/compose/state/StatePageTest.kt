package com.yikwing.compose.state

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.yikwing.compose.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class StatePageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shouldRetryFromBlankErrorThroughLoadingToContent() {
        val state = mutableStateOf<PageState>(PageState.Error(" \n\t"))
        val onRetry = mutableStateOf<(() -> Unit)?>(null)
        var retries = 0
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val errorMessage = context.getString(R.string.yk_compose_error)
        val retryLabel = context.getString(R.string.yk_compose_retry)

        composeRule.setContent {
            MaterialTheme {
                StatePage(state = state.value, onRetry = onRetry.value) {
                    Text("Loaded result")
                }
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeRule.onNodeWithText(retryLabel).assertDoesNotExist()
        composeRule.onNodeWithText("Loaded result").assertDoesNotExist()

        composeRule.runOnIdle {
            onRetry.value = {
                retries++
                state.value = PageState.Loading
            }
        }
        composeRule.onNodeWithText(retryLabel).performClick()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        composeRule.onNodeWithText(errorMessage).assertDoesNotExist()
        composeRule.runOnIdle {
            assertThat(retries, `is`(1))
            state.value = PageState.Content
        }
        composeRule.onNodeWithText("Loaded result").assertIsDisplayed()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun shouldUseCustomSlotsAndDisposeContentWhenLeavingContentState() {
        val state = mutableStateOf<PageState>(PageState.Content)
        var disposals = 0

        composeRule.setContent {
            MaterialTheme {
                StatePage(
                    state = state.value,
                    loadingContent = { Text("Custom loading") },
                    emptyContent = { Text("No favorites") },
                    errorContent = { message -> Text("Custom error: $message") },
                ) {
                    DisposableEffect(Unit) {
                        onDispose { disposals++ }
                    }
                    Text("Loaded result")
                }
            }
        }

        composeRule.onNodeWithText("Loaded result").assertIsDisplayed()
        composeRule.runOnIdle { state.value = PageState.Loading }
        composeRule.onNodeWithText("Custom loading").assertIsDisplayed()
        composeRule.onNodeWithText("Loaded result").assertDoesNotExist()
        composeRule.runOnIdle {
            assertThat(disposals, `is`(1))
            state.value = PageState.Empty
        }
        composeRule.onNodeWithText("No favorites").assertIsDisplayed()
        composeRule.onNodeWithText("Custom loading").assertDoesNotExist()

        composeRule.runOnIdle { state.value = PageState.Error("Service unavailable") }
        composeRule.onNodeWithText("Custom error: Service unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("No favorites").assertDoesNotExist()
    }
}
