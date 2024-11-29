package ie.equalit.ceno.ui

import ie.equalit.ceno.helpers.AndroidAssetDispatcher
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.ui.robots.homepage
import ie.equalit.ceno.ui.robots.onboarding
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomepageTest {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(3)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
        onboarding {
        }.skipOnboardingIfNeeded()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun cenoNetworkStatusIconTest() {
        homepage {
            verifyCenoNetworkStatusIcon()
            openCenoNetworkStatusDialog()
            verifyCenoNetworkStatusDialog()
        }
    }
}