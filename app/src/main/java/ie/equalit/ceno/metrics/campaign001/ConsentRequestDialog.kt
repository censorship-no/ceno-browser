package ie.equalit.ceno.metrics.campaign001

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.dialogs.WebViewPopupPanel

class ConsentRequestDialog(val context: Context) {

    fun show(complete: (Boolean) -> Unit) {

        val privacyPolicyUrl = context.getString(R.string.privacy_policy_url)

        val dialogView = View.inflate(context, R.layout.dialog_metrics_campaign001, null)
        dialogView.findViewById<TextView>(R.id.learn_more).setOnClickListener {
            val dialog = WebViewPopupPanel(context, context as LifecycleOwner, privacyPolicyUrl)
            dialog.show()
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
