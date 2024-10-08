package ie.equalit.ceno.metrics.campaign001

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.dialogs.WebViewPopupPanel

class ConsentRequestDialog(val context: Context) {

    fun show(complete: (Boolean) -> Unit) {

        val privacyPolicyUrl = context.getString(R.string.privacy_policy_url)

        val dialogView = View.inflate(context, R.layout.clean_insights_nudge_dialog, null)
        dialogView.findViewById<TextView>(R.id.learn_more).setOnClickListener {
            val dialog = WebViewPopupPanel(context, context as LifecycleOwner, privacyPolicyUrl)
            dialog.show()
        }

        val deviceType = dialogView.findViewById<View>(R.id.checkbox) as CheckBox
        deviceType.isChecked = Settings.isCleanInsightsDeviceTypeIncluded(context)
        deviceType.setOnCheckedChangeListener { _, isChecked ->
            // Save to shared preferences
            Settings.setCleanInsightsDeviceType(context, isChecked)
        }

        val deviceLocale = dialogView.findViewById<View>(R.id.checkbox2) as CheckBox
        deviceLocale.isChecked = Settings.isCleanInsightsDeviceLocaleIncluded(context)
        deviceLocale.setOnCheckedChangeListener { _, isChecked ->
            // Save to shared preferences
            Settings.setCleanInsightsDeviceLocale(context, isChecked)
        }

        var selectionMade = false
        AlertDialog.Builder(context)
            .setView(dialogView)
            .setNegativeButton(R.string.clean_insights_maybe_later) { _, _ ->
                selectionMade = true
                complete(false)
            }
            .setPositiveButton(R.string.clean_insights_opt_in) { _, _ ->
                selectionMade = true
                complete(true)
            }
            .setOnDismissListener {
                if (!selectionMade) {
                    /* No selection was made, but dialog was dismissed
                        do not grant consent */
                    complete(false)
                }
            }
            .create()
            .show()
    }
}
