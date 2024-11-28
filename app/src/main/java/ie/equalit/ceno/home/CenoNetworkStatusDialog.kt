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
                dialog.cancel()
            }
            setIcon(icon)
            setOnDismissListener(onDismissListener)
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}