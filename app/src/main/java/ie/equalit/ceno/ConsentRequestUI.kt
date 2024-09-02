package ie.equalit.ceno

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import ie.equalit.ceno.ext.click
import ie.equalit.ceno.ext.components
import org.cleaninsights.sdk.Feature

class ConsentRequestUi(private val context: Context) {

    fun show(complete: (Boolean) -> Unit) {

        val dialogView = View.inflate(context, R.layout.clean_insights_nudge_dialog, null)
        dialogView.findViewById<TextView>(R.id.explainer).text = buildSpannedString {

            append(context.getString(R.string.clean_insights_explainer))

//            color(ContextCompat.getColor(context, R.color.accent)) {
//                click(false, onClick = {
//                    // todo: open popup?
//                }) {
//                    append(" ")
//                    append(context.getString(R.string.learn_more_title))
//                    append(".")
//                }
//            }
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setNegativeButton(R.string.clean_insights_maybe_later) { _, _ ->
                complete(false)
            }
            .setPositiveButton(R.string.clean_insights_opt_in) { _, _ ->
                complete(true)
            }
            .setOnDismissListener {
                complete(false)
            }
            .create()
            .show()
    }

    fun show(feature: Feature, complete: (Boolean) -> Unit) {
        val msg = context.getString(
            R.string.clean_insights_feature_consent_explanation,
            feature.localized(context)
        )

        AlertDialog.Builder(context)
            .setTitle(R.string.clean_insights_header)
            .setMessage(msg)
            .setNegativeButton(R.string.clean_insights_no) { _, _ -> complete(false) }
            .setPositiveButton(R.string.ceno_notification_clear_do_description) { _, _ ->
                complete(true)
            }
            .create()
            .show()
    }
}

fun Feature.localized(context: Context): String {
    return when (this) {
        Feature.Lang -> context.getString(R.string.clean_insights_locale)
        Feature.Ua -> context.getString(R.string.clean_insights_device_type)
    }
}