package ie.equalit.ceno.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.AndroidAssetDispatcher
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestHelper
import ie.equalit.ceno.ui.robots.OnboardingRobot
import ie.equalit.ceno.ui.robots.givePermissions
import ie.equalit.ceno.ui.robots.onboarding
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class TooltipsTest {
    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(1)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
        mDevice.wait(Until.gone(By.res("${TestHelper.packageName}:id/iv_standby_logo")), TestAssetHelper.waitingTime)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Ignore("Custom matcher needed for verifying text within the tooltip")
    @Test
    fun allTooltipsTest() {
        onboarding {
            verifyStartTooltipExists()
            verifyStartTooltipText()
            verifyStartTooltipButtons()
            beginTooltipsTour()
            verifyPublicPersonalTooltip()
//            goToNextTooltip()
//            verifyShortcutsTooltip()
//            goToNextTooltip()
//            verifyUrlTooltip()
//            goToNextTooltip()
//            verifySourcesTooltip()
//            goToNextTooltip()
//            verifyClearTooltip()
//            goToNextTooltip()
//            verifyPermissionsTooltip()
//            givePermissions()
        }
    }

    @Test
    fun skipAllTooltips() {
        onboarding {
        }.skipOnboardingIfNeeded()
    }

    @Test
    fun exitTooltip() {
        onboarding {
            verifyStartTooltipExists()
            verifyStartTooltipButtons()
            beginTooltipsTour()
            verifyPublicPersonalTooltip()
            verifyExitButton()
            clickExit()
            verifyPermissionsTooltip()
            clickPermissions()
            givePermissions()
        }
    }

}