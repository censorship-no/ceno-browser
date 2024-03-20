package ie.equalit.ceno.ui.robots

import android.view.KeyEvent
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.CoreMatchers.allOf

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewNetworkDetailsRobot {


    fun verifySourcesUpButton() = assertNetworkDetailsUpButton()
    fun verifyNetworkDetailsSettings() = assertNetworkDetailsSettingsView()

    fun verifyGeneralHeading(): ViewInteraction = assertGeneralHeading()
    fun verifyOuinetProtocolDisplay(): ViewInteraction = assertOuinetProtocolDisplay()
    fun verifyReachabilityStatusDisplay(): ViewInteraction = assertReachabilityStatusDisplay()
    fun verifyUpnpStatusDisplay(): ViewInteraction = assertUpnpStatusDisplay()

    fun verifyUdpHeading(): ViewInteraction = assertUdpHeading()
    fun verifyLocalUdpEndpointsDisplay(): ViewInteraction = assertLocalUdpEndpointsDisplay()
    fun verifyExternalUdpEndpointsDisplay(): ViewInteraction = assertExternalUdpEndpointsDisplay()
    fun verifyPublicUdpEndpointsDisplay(): ViewInteraction = assertPublicUdpEndpointsDisplay()

    fun verifyBridgeModeHeading(): ViewInteraction = assertBridgeModeHeading()
    fun verifyBridgeModeToggle(): ViewInteraction = assertBridgeModeToggle()
    fun verifyBridgeModeSummary(): ViewInteraction = assertBridgeModeSummary()

    fun verifyBtBootstrapsHeading(): ViewInteraction = assertBtBootstrapsHeading()
    fun verifyExtraBtBootstrapsButton(): ViewInteraction = assertExtraBtBootstrapsButton()


    fun clickBridgeModeToggle() = bridgeModeToggle().click()

    fun waitForBridgeModeDialog() {
        mDevice.findObject(
            UiSelector()
                .textContains("Updating Bridge Mode settings"),
        ).waitUntilGone(TestAssetHelper.waitingTime)
    }

    fun clickDownRecyclerView(count: Int) {
        for (i in 1..count) {
            recycleView().perform(ViewActions.pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        }
    }

    class Transition {
        fun settingsViewNetworkDetails(interact: SettingsViewNetworkDetailsRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun networkDetailsUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))

private fun networkDetailsSettingsView() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_network_config))

private fun recycleView() = Espresso.onView(ViewMatchers.withId(R.id.recycler_view))

private fun generalHeading() = Espresso.onView(ViewMatchers.withText(R.string.general_category))
private fun ouinetProtocolDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_about_ouinet_protocol))
private fun reachabilityStatusDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_reachability))
private fun upnpStatusDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_upnp))

private fun udpHeading() = Espresso.onView(ViewMatchers.withText(R.string.udp_category))
private fun localUdpEndpointsDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_local_udp))
private fun externalUdpEndpointsDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_external_udp))
private fun publicUdpEndpointsDisplay() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_public_udp))

private fun bridgeModeHeading() = Espresso.onView(ViewMatchers.withText(R.string.bridge_mode_category))
private fun bridgeModeToggle() = Espresso.onView(allOf(ViewMatchers.withId(R.id.switchWidget), hasCousin(ViewMatchers.withText(R.string.preferences_ceno_bridge_announcement))))
private fun bridgeModeSummary() = Espresso.onView(ViewMatchers.withText(R.string.bridge_mode_ip_warning_text))

private fun btBootstrapsHeading() = Espresso.onView(ViewMatchers.withText(R.string.bit_torrent_bootstraps_category))
private fun extraBtBootstrapsButton() = Espresso.onView(ViewMatchers.withText(R.string.preferences_ceno_sources_extra_bitTorrent_bootstraps))

private fun assertNetworkDetailsUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertNetworkDetailsSettingsView() = networkDetailsSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertGeneralHeading() = generalHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertOuinetProtocolDisplay() = ouinetProtocolDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertReachabilityStatusDisplay() = reachabilityStatusDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertUpnpStatusDisplay() = upnpStatusDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertUdpHeading() = udpHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertLocalUdpEndpointsDisplay() = localUdpEndpointsDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertExternalUdpEndpointsDisplay() = externalUdpEndpointsDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertPublicUdpEndpointsDisplay() = publicUdpEndpointsDisplay()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBridgeModeHeading() = bridgeModeHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBridgeModeToggle() = bridgeModeToggle()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBridgeModeSummary() = bridgeModeSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBtBootstrapsHeading() = btBootstrapsHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertExtraBtBootstrapsButton() = extraBtBootstrapsButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
