package ie.equalit.ceno.settings

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R

class UpdateBridgeAnnouncementDialog(
    context: Context
) {
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        val dialogView = View.inflate(context, R.layout.bridge_announcement_dialog, null)
        builder.apply {
            setTitle(ContextCompat.getString(context, R.string.bridge_announcement_dialog_title))
            setView(dialogView)
            setCancelable(false)
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create().apply {
            setCanceledOnTouchOutside(false)
        }
    }
}