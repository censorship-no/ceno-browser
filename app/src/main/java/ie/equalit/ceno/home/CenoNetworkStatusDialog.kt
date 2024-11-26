package ie.equalit.ceno.home

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import ie.equalit.ceno.R
import ie.equalit.ouinet.Ouinet.RunningState

class CenoNetworkStatusDialog(
    val context: Context,
    val status: RunningState,
    onDismissListener: DialogInterface.OnDismissListener
){
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    init {
        val message:Int = when(status) {
            RunningState.Started -> R.string.ceno_network_status_connected
            RunningState.Degraded -> R.string.ceno_network_status_degraded
            else -> R.string.ceno_network_status_disconnected
        }
        builder.apply {
            setTitle(R.string.ceno_network_status_title)
            setMessage(message)
            setPositiveButton(R.string.dialog_btn_positive_ok) { dialog, _ ->
                dialog.cancel()
            }
            setOnDismissListener(onDismissListener)
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}