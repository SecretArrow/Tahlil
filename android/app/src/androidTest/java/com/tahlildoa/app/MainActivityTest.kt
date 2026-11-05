package com.tahlildoa.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for the app's launch path.
 *
 * These tests run on a real device or emulator under instrumentation.
 * They are the regression net for the launch-time crash that
 * prompted the ProGuard / theme / manifest fixes — if any of those
 * regressions come back, `appLaunchesWithoutCrash` will fail in CI
 * before a release can ship.
 *
 * The Compose test rule waits for the recomposition to settle before
 * each assertion, so we don't need explicit `waitForIdle()` calls —
 * but we keep one in `appLaunchesWithoutCrash` to make the intent
 * explicit.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesWithoutCrash() {
        // If the app crashes on launch, this test fails — which is
        // exactly the regression net we want.
        composeRule.waitForIdle()
    }

    @Test
    fun heroTitleIsDisplayed() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Tahlil Singkat").assertExists()
    }

    @Test
    fun ctaButtonsAreDisplayed() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Mulai").assertExists()
        composeRule.onNodeWithText("Tahlil").assertExists()
        composeRule.onNodeWithText("Dzikir").assertExists()
        composeRule.onNodeWithText("Doa").assertExists()
    }

    @Test
    fun tappingMulaiNavigatesToPembukaan() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Mulai").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pembukaan").assertExists()
    }
}
