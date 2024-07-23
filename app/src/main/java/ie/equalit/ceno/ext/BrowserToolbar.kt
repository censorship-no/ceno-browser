package ie.equalit.ceno.ext

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ie.equalit.ceno.R
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.ui.widgets.behavior.EngineViewClippingBehavior
import mozilla.components.ui.widgets.behavior.EngineViewScrollingBehavior
import mozilla.components.ui.widgets.behavior.ToolbarPosition as engineToolbarPosition
import mozilla.components.ui.widgets.behavior.ViewPosition as browserToolbarPosition

/**
 * NOTE: This was adapted from Firefox Focus, please reference original code if API changes are made,
 * https://github.com/mozilla-mobile/firefox-android/blob/main/focus-android/app/src/main/java/org/mozilla/focus/ext/BrowserToolbar.kt
 *
 * Collapse the toolbar and block it from appearing until calling [enableDynamicBehavior].
 * Useful in situations like entering fullscreen.
 *
 * @param engineView [EngineView] previously set to react to toolbar's dynamic behavior.
 * Will now go through a bit of cleanup to ensure everything will be displayed nicely even without a toolbar.
 */
fun BrowserToolbar.disableDynamicBehavior(engineView: EngineView, shouldUseTopToolbar: Boolean) {
    (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = null
    (layoutParams as? CoordinatorLayout.LayoutParams)?.gravity = if (shouldUseTopToolbar) {
        Gravity.TOP
    } else {
        Gravity.BOTTOM
    }

    engineView.setDynamicToolbarMaxHeight(0)
    engineView.asView().translationY = if (shouldUseTopToolbar) {
        context.resources.getDimension(R.dimen.browser_toolbar_height)
    } else {
        0f
    }
    (engineView.asView().layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = null
}

/**
 * Expand the toolbar and reenable the dynamic behavior.
 * Useful after [disableDynamicBehavior] for situations like exiting fullscreen.
 *
 * @param context [Context] used in setting up the dynamic behavior.
 * @param engineView [EngineView] that should react to toolbar's dynamic behavior.
 */
fun BrowserToolbar.enableDynamicBehavior(
    context: Context,
    engineView: EngineView,
    shouldUseTopToolbar: Boolean
) {
    (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = EngineViewScrollingBehavior(
        context,
        null,
        if (shouldUseTopToolbar) browserToolbarPosition.TOP else browserToolbarPosition.BOTTOM,
    )
    (layoutParams as? CoordinatorLayout.LayoutParams)?.gravity = if (shouldUseTopToolbar) {
        Gravity.TOP
    } else {
        Gravity.BOTTOM
    }

    val toolbarHeight = context.resources.getDimension(R.dimen.browser_toolbar_height).toInt()
    engineView.setDynamicToolbarMaxHeight(toolbarHeight)
    (engineView.asView().layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
        topMargin = 0
        behavior = EngineViewClippingBehavior(
            context,
            null,
            engineView.asView(),
            toolbarHeight,
            if (shouldUseTopToolbar) engineToolbarPosition.TOP else engineToolbarPosition.BOTTOM,
        )
    }
}

/**
 * Show this toolbar at the top of the screen, fixed in place, with the EngineView immediately below it.
 *
 * @param context [Context] used for various system interactions
 * @param engineView [EngineView] that must be shown immediately below the toolbar.
 */
fun BrowserToolbar.showAsFixed(context: Context, engineView: EngineView) {
    visibility = View.VISIBLE

    engineView.setDynamicToolbarMaxHeight(0)

    val toolbarHeight = context.resources.getDimension(R.dimen.browser_toolbar_height).toInt()
    (engineView.asView().layoutParams as? CoordinatorLayout.LayoutParams)?.topMargin = toolbarHeight
}

/**
 * Remove this toolbar from the screen and allow the EngineView to occupy the entire screen.
 *
 * @param engineView [EngineView] that will be configured to occupy the entire screen.
 */
fun BrowserToolbar.hide(engineView: EngineView) {
    engineView.setDynamicToolbarMaxHeight(0)
    (engineView.asView().layoutParams as? CoordinatorLayout.LayoutParams)?.topMargin = 0

    visibility = View.GONE
}