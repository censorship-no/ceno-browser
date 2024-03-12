package ie.equalit.ceno.ui.robots

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestHelper
import ie.equalit.ceno.settings.Settings

class OnboardingRobot {

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun givePermissionsIfNeeded(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                continueOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
                continueOnboardingButton().click()
                givePermissions()
            }
        }
        fun skipOnboardingIfNeeded() {
            if (Settings.shouldShowOnboarding(TestHelper.appContext)) {
                skipOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
                skipOnboardingButton().click()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    givePermissions()
                }
            }
        }
    }
}

fun onboarding(interact: OnboardingRobot.() -> Unit): OnboardingRobot.Transition {
    OnboardingRobot().interact()
    return OnboardingRobot.Transition()
}

fun clickContinue() {
    continueOnboardingButton().click()
}
fun waitForContinueButton() {
    continueOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
}

private fun continueOnboardingButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_onboarding_continue"),
)

private fun skipOnboardingButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_onboarding_skip"),
)

private fun permissionAllowButton() = mDevice.findObject(
    UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button")
)

private fun permissionDenyButton() = mDevice.findObject(
    UiSelector().resourceId("com.android.permissioncontroller:id/permission_deny_button")
)

private fun backgroundAllowButton() = mDevice.findObject(
UiSelector().resourceId("android:id/button1")
)

private fun backgroundDenyButton() = mDevice.findObject(
    UiSelector().resourceId("android:id/button2")
)


fun givePermissions() {
    //for allowing notifications
    permissionAllowButton().waitForExists(TestAssetHelper.waitingTime)
    permissionAllowButton().click()
    //for battery optimizations
    backgroundAllowButton().waitForExists(TestAssetHelper.waitingTime)
    backgroundAllowButton().click()
}

fun denyPermissions() {
    //for allowing notifications
    permissionDenyButton().waitForExists(TestAssetHelper.waitingTime)
    permissionDenyButton().click()
    //for battery optimizations
    backgroundDenyButton().waitForExists(TestAssetHelper.waitingTime)
    backgroundDenyButton().click()
}
