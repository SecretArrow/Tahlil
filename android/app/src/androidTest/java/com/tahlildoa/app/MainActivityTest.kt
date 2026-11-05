package com.tahlildoa.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
 * They are the regression net for the launch-time crash that prompted the
 * ProGuard / theme / manifest fixes — if any of those regressions come
 * back, `appLaunchesWithoutCrash` will fail in CI before a release can
 * ship.
 *
 * The Compose test rule waits for recomposition to settle before each
 * assertion, but we add an explicit `waitUntil` for the hero title to
 * give DataStore (which loads asynchronously) time to publish the
 * initial state.
 *
 * Note: The navigation drawer (ModalNavigationDrawer) is mounted at the
 * root and may render section titles like "Pembukaan" in the drawer
 * list even when the drawer is closed. To avoid ambiguous-node errors,
 * we use `onAllNodesWithText(...)[0]` (the first match) for texts that
 * might appear in multiple places, and assert `isDisplayed()` so we
 * only match nodes the user can actually see.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesWithoutCrash() {
        // The most important test — if the app crashes on launch, this
        // fails. The compose rule's waitForIdle() will throw if the
        // activity throws during onCreate or initial composition.
        composeRule.waitForIdle()
    }

    @Test
    fun heroTitleIsDisplayed() {
        // Wait up to 10s for the hero title to appear (DataStore
        // loads asynchronously and the home screen renders only after
        // the initial uiState is published).
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText("Tahlil Singkat").fetchSemanticsNodes().isNotEmpty()
        }
        // Verify at least one is displayed (the hero title in the
        // LazyColumn item).
        composeRule.onAllNodesWithText("Tahlil Singkat")[0].assertIsDisplayed()
    }

    @Test
    fun ctaButtonsAreDisplayed() {
        // Wait for the CTA buttons to appear.
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText("Mulai").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("Mulai")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("Tahlil")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("Dzikir")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("Doa")[0].assertIsDisplayed()
    }

    @Test
    fun tappingMulaiNavigatesToPembukaan() {
        // Wait for the hero CTA buttons to be ready.
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText("Mulai").fetchSemanticsNodes().isNotEmpty()
        }
        // Tap the first "Mulai" (the hero CTA — the drawer is closed
        // so there's no other "Mulai" node visible).
        composeRule.onAllNodesWithText("Mulai")[0].performClick()
        // Wait for the section detail screen to render.
        composeRule.waitForIdle()
        // After navigation, "Pembukaan" appears in the section title
        // (and possibly the drawer). Assert at least one is displayed.
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("Pembukaan", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("Pembukaan", substring = true)[0]
            .assertIsDisplayed()
    }
}
