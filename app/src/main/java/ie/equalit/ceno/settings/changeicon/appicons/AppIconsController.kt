package ie.equalit.ceno.settings.changeicon.appicons

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings

interface AppIconsController {
    fun handleAppIconClicked(icon: AppIcon)
}

class DefaultAppIconsController(
    private val activity: BrowserActivity,
    private val appIconModifier: AppIconModifier
) : AppIconsController {


    private fun onIconConfirmed(icon: AppIcon) {
        val context = activity
        val previousIcon = Settings.appIcon(context)
        val newIcon = AppIcon.from(icon.componentName)
        Settings.setAppIcon(context, icon.componentName)
        appIconModifier.changeIcon(previousIcon!!, newIcon)
    }

    @SuppressLint("InflateParams")
    override fun handleAppIconClicked(icon: AppIcon) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.changeIconDialogTitle)
            .setMessage(activity.getString(R.string.changeIconDialogMessage))
            .setPositiveButton(R.string.changeIconCtaAccept) { _, _ ->
                onIconConfirmed(icon)
            }
            .setNegativeButton(R.string.changeIconCtaCancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
