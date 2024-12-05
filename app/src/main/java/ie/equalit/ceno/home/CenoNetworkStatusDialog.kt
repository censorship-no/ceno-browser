package ie.equalit.ceno.home

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.ExportAndroidLogsDialog
import ie.equalit.ouinet.Ouinet.RunningState

class CenoNetworkStatusDialog(
    val context: Context,
    val fragment: Fragment,
    val status: RunningState,
    onDismissListener: DialogInterface.OnDismissListener
){
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
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
            if (status != RunningState.Started && status != RunningState.Degraded) {
                //add additional buttons
//                setNeutralButton("SUPPORT", {dialog,_ ->
//                    dialog.cancel()
//                })
                setNeutralButton("EXPORT LOGS", {dialog,_ ->
                    dialog.dismiss()
                    ExportAndroidLogsDialog(context, fragment).getDialog().show()
                })
            }
            setIcon(icon)
            setOnDismissListener(onDismissListener)
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}