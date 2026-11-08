package com.tahlil.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
 * They are the regression net for the launch-time crash that prompted
 * the ProGuard / theme / manifest fixes.
 *
 * The most important test is [appLaunchesWithoutCrash] — if the app
 * crashes on launch, this test fails and blocks the release.
 *
 * [tappingMulaiNavigatesToFirstSection] verifies that the home screen
 * renders the "Mulai Membaca" CTA AND that tapping it navigates to the
 * section detail screen.
 *
 * Note on the drawer TOC: the home screen's ModalNavigationDrawer keeps
 * its TOC (which lists every section title) composed even while the
 * drawer is closed — it's just positioned off-screen. That means
 * `onAllNodesWithText("<section title>")` returns at least two nodes
 * (drawer copy + on-screen copy) the moment sections load, and
 * `assertIsDisplayed()` on `[0]` may hit the off-screen drawer copy.
 * The navigation assertion below deliberately uses the home-only
 * "Mulai Membaca" button instead, which has no drawer counterpart.
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
    fun tappingMulaiNavigatesToFirstSection() {
        // 1. Wait for the home screen to render. The hero's "Mulai
        //    Membaca" button is composed unconditionally — but it is
        //    DISABLED (enabled = firstSectionId != null) until the
        //    variant's sections have loaded. We must wait for it to be
        //    ENABLED before tapping, otherwise performClick() is a silent
        //    no-op on a disabled button and navigation never fires.
        composeRule.waitUntil(timeoutMillis = 30_000L) {
            try {
                composeRule.onAllNodesWithText("Mulai Membaca").onFirst().assertIsEnabled()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // 2. Tap "Mulai Membaca" — the button is enabled (firstSectionId
        //    is set), so onClick fires onSectionClick(firstSectionId) and
        //    navigates to the section detail screen.
        composeRule.onAllNodesWithText("Mulai Membaca")[0].performClick()

        // 3. Assert navigation succeeded: the home-only "Mulai Membaca"
        //    button is no longer DISPLAYED. We check "not displayed"
        //    rather than "not present" because Compose Navigation may
        //    retain the previous destination's composition in the back
        //    stack (state-saved) — if so, the button node still exists
        //    but is off-screen. Either way (gone entirely, or present
        //    but off-screen), navigation happened. If the button is
        //    still displayed, navigation did not happen and the test
        //    fails with a timeout.
        composeRule.waitUntil(timeoutMillis = 15_000L) {
            noNodeDisplayed("Mulai Membaca")
        }
    }

    /**
     * Returns true if no node with the given text is currently displayed.
     * Used to confirm navigation away from a screen: the source screen's
     * unique UI is either removed from composition entirely, or retained
     * in the back stack but off-screen.
     */
    private fun noNodeDisplayed(text: String): Boolean {
        val nodes = composeRule.onAllNodesWithText(text).fetchSemanticsNodes()
        if (nodes.isEmpty()) return true
        return (0 until nodes.size).none { i ->
            try {
                composeRule.onAllNodesWithText(text)[i].assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
