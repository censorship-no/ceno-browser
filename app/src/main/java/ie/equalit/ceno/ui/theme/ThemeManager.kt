package ie.equalit.ceno.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.ContextThemeWrapper
import android.view.Window
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.view.createWindowInsetsController

abstract class ThemeManager {
    abstract var currentMode: BrowsingMode


    /**
     * Handles status bar theme change since the window does not dynamically recreate
     */

    abstract fun applyStatusBarTheme()

    fun updateNavigationBar(window: Window, context: Context) {
        window.navigationBarColor = context.getColorFromAttr(R.attr.layer1)
    }

    fun clearLightSystemBars(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.createWindowInsetsController().isAppearanceLightStatusBars = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API level can display handle light navigation bar color
            window.createWindowInsetsController().isAppearanceLightNavigationBars = false
        }
    }

    abstract fun applyTheme(toolbar: BrowserToolbar)

    fun updateLightSystemBars(window: Window, context: Context) {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = context.getColorFromAttr(R.attr.layer1)
            window.createWindowInsetsController().isAppearanceLightStatusBars = true
        } else {
            window.statusBarColor = Color.BLACK
        }

        if (SDK_INT >= Build.VERSION_CODES.O) {
            // API level can display handle light navigation bar color
            window.createWindowInsetsController().isAppearanceLightNavigationBars = true
            updateNavigationBar(window, context)
        }
    }

    fun updateDarkSystemBars(window: Window, context: Context) {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = context.getColorFromAttr(R.attr.layer1)
            window.createWindowInsetsController().isAppearanceLightStatusBars = false
        } else {
            window.statusBarColor = Color.BLACK
        }

        if (SDK_INT >= Build.VERSION_CODES.O) {
            // API level can display handle light navigation bar color
            window.createWindowInsetsController().isAppearanceLightNavigationBars = false
            updateNavigationBar(window, context)
        }
    }

    abstract fun applyStatusBarThemeTabsTray()

    abstract fun getContext(): Context
    abstract fun getIconColor(): Int
}

class DefaultThemeManager(
    mode: BrowsingMode,
    private val activity: Activity
) : ThemeManager() {

    private var currentContext: Context = activity

    private val personalThemeContext = ContextThemeWrapper(activity, R.style.PersonalTheme)

    override var currentMode: BrowsingMode = mode
        set(value) {
            //apply theme to fragment rather than recreating activity
            field = value
            if (value == BrowsingMode.Personal)
                currentContext = personalThemeContext
            else
                currentContext = activity
        }

    override fun applyStatusBarThemeTabsTray() {
        when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO,
            -> {
                updateLightSystemBars(activity.window, activity)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED, //we assume dark mode is default
            Configuration.UI_MODE_NIGHT_YES -> {
                updateDarkSystemBars(activity.window, activity)
            }
        }
    }

    override fun getContext(): Context {
        return currentContext
    }

    override fun getIconColor(): Int {
        if (currentMode.isPersonal)
            return R.color.fx_mobile_private_icon_color_primary
        else
            return R.color.fx_mobile_icon_color_primary
    }

    override fun applyStatusBarTheme() {
        when (currentMode) {
            BrowsingMode.Normal -> {
                when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO,
                    -> {
                        updateLightSystemBars(activity.window, currentContext)
                    }

                    Configuration.UI_MODE_NIGHT_UNDEFINED, //we assume dark mode is default
                    Configuration.UI_MODE_NIGHT_YES -> {
                        updateDarkSystemBars(activity.window, currentContext)
                    }
                }
            }

            BrowsingMode.Personal -> {
                updateDarkSystemBars(activity.window, currentContext)
            }
        }
    }

    override fun applyTheme(toolbar: BrowserToolbar) {
        applyStatusBarTheme()

        toolbar.background =
            ContextCompat.getDrawable(currentContext, R.drawable.toolbar_dark_background)

        var textPrimary = ContextCompat.getColor(
            currentContext,
            currentContext.theme.resolveAttribute(R.attr.textPrimary)
        )
        var textSecondary = ContextCompat.getColor(
            currentContext,
            currentContext.theme.resolveAttribute(R.attr.textSecondary)
        )

        toolbar.edit.colors = toolbar.edit.colors.copy(
            text = textPrimary,
            hint = textSecondary
        )
        toolbar.display.colors = toolbar.display.colors.copy(
            text = textPrimary,
            hint = textSecondary,
            securityIconSecure = textPrimary,
            securityIconInsecure = textPrimary,
            menu = textPrimary,

            )

        /*
        * When switching between modes, we need to set the url background to something else before
        * before setting it to the correct url background
        * */
        toolbar.display.setUrlBackground(
            ContextCompat.getDrawable(
                currentContext,
                R.drawable.toolbar_dark_background
            )
        )
        toolbar.display.setUrlBackground(
            ContextCompat.getDrawable(
                currentContext,
                R.drawable.url_background
            )
        )
    }

}