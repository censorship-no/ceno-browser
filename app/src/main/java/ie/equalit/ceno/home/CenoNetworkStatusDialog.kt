package ie.equalit.ceno.home

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.AboutFragment
import ie.equalit.ceno.settings.ExportAndroidLogsDialog
import ie.equalit.ouinet.Ouinet.RunningState

class CenoNetworkStatusDialog(
    val context: Context,
    val fragment: Fragment,
    val status: RunningState,
    onDismissListener: DialogInterface.OnDismissListener
){
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private lateinit var alertDialog: AlertDialog
    init {
        val message:Int
        val icon:Int
        when(status) {
            RunningState.Started -> {
                message = R.string.ceno_network_status_connected
                icon = R.drawable.ceno_connected_icon
            }
            RunningState.Degraded -> {
                message = R.string.ceno_network_status_degraded
                icon = R.drawable.ceno_degraded_icon
            }
            else -> {
                message = R.string.ceno_network_status_disconnected
                icon = R.drawable.ceno_disconnected_icon
            }
        }
        builder.apply {
            setTitle(R.string.ceno_network_status_title)
            setMessage(message)
            setPositiveButton(R.string.dialog_btn_positive_ok) { dialog, _ ->
                dialog.dismiss()
            }

            if (status != RunningState.Started) {
                //add additional buttons
                setNegativeButton("EXPORT LOGS") { dialog, _ ->
                    dialog.dismiss()
                    ExportAndroidLogsDialog(context, fragment).getDialog().show()
                }
                setView(getContactSupportView())
            }
            setIcon(icon)
            setOnDismissListener(onDismissListener)
        }
    }

    private fun getContactSupportView(): View {
        val view = View.inflate(context, R.layout.ceno_status_contact_support_dialog, null)
        val btnContactSupport = view.findViewById<TextView>(R.id.btn_contact_support)
        AboutFragment.setLinkTextView(context, btnContactSupport, ContextCompat.getString(context, R.string.contact_support))
        btnContactSupport.setOnClickListener {

            alertDialog.dismiss()
            //Add mailto link to support@censorship.no
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                setData(Uri.parse("mailto:"))
                putExtra(Intent.EXTRA_EMAIL, "support@censorship.no")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(context, intent, null)
            }
        }
        return view
    }

    fun getDialog(): AlertDialog {
        alertDialog = builder.create()
        return alertDialog
    }
}