package ie.equalit.ceno.settings

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.NavGraphDirections
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getSizeInMB
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.home.HomeFragment
import ie.equalit.ceno.settings.SettingsFragment.Companion.LOGS_LAST_10_MINUTES
import ie.equalit.ceno.settings.SettingsFragment.Companion.LOGS_LAST_5_MINUTES
import ie.equalit.ceno.settings.SettingsFragment.Companion.TAG
import ie.equalit.ceno.standby.StandbyFragment
import ie.equalit.ceno.utils.LogReader
import ie.equalit.ouinet.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.engine.prompt.ShareData

class ExportAndroidLogsDialog (
    val context: Context,
    val fragment: Fragment,
    var onDismiss: () -> Unit = {}
) {
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var job: Job? = null

    init {
        val logTimeFilterDialogView = View.inflate(context, R.layout.select_logtime_filter, null)
        val radio5Button = logTimeFilterDialogView.findViewById<RadioButton>(R.id.radio_5_minutes)
        val radio10Button = logTimeFilterDialogView.findViewById<RadioButton>(R.id.radio_10_minutes)
        val checkboxDebugLogs = logTimeFilterDialogView.findViewById<CheckBox>(R.id.checkBox_debug_logs)

        if(fragment is StandbyFragment || fragment is HomeFragment) {
            checkboxDebugLogs.visibility = View.VISIBLE
        } else {
            checkboxDebugLogs.visibility = View.GONE
        }

        builder.apply {
            setTitle(R.string.select_log_scope_header)
            setMessage(R.string.select_log_scope_message)
            setView(logTimeFilterDialogView)
            setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.onboarding_battery_button) { _, _ ->

                //enable debug logs
                if (checkboxDebugLogs.isVisible) {
                    //check if logfile is enabled
                    if (checkboxDebugLogs.isChecked != CenoSettings.isCenoLogEnabled(context = this@ExportAndroidLogsDialog.context)) {
                        // network request to update preference value
                        CenoSettings.ouinetClientRequest(
                            context = this@ExportAndroidLogsDialog.context,
                            key = OuinetKey.LOGFILE,
                            newValue = if (checkboxDebugLogs.isChecked) OuinetValue.ENABLED else OuinetValue.DISABLED,
                            stringValue = null,
                            object : OuinetResponseListener {
                                override fun onSuccess(message: String, data: Any?) {
                                    CenoSettings.setCenoEnableLog(this@ExportAndroidLogsDialog.context, checkboxDebugLogs.isChecked)
                                }

                                override fun onError() {
                                    Log.e(TAG, "Failed to set log file to newValue: ${checkboxDebugLogs.isChecked}")
                                }
                            }
                        )

                        // network request to update log level based on preference value
                        CenoSettings.ouinetClientRequest(
                            context = this@ExportAndroidLogsDialog.context,
                            key = OuinetKey.LOG_LEVEL,
                            stringValue = if (checkboxDebugLogs.isChecked) Config.LogLevel.DEBUG.toString() else Config.LogLevel.INFO.toString(),
                            ouinetResponseListener = object : OuinetResponseListener {
                                override fun onSuccess(message: String, data: Any?) {
                                    //restart ouinet
                                    fragment.requireComponents.ouinet.background.restartOuinet()
                                }

                                override fun onError() {
                                    Log.e(TAG, "Failed to set log file to newValue: ${checkboxDebugLogs.isChecked}")
                                }

                            }
                        )

                    }
                }

                // Initialize Android logs

                var logs: MutableList<String>
                var logString: String

                val progressDialogView = View.inflate(context, R.layout.progress_dialog, null)
                val progressView = progressDialogView.findViewById<ProgressBar>(R.id.progress_bar)
                val progressDialog = getProgressDialog(progressDialogView)

                job = fragment.viewLifecycleOwner.lifecycleScope.launch {

                    withContext(Dispatchers.Main) {

                        progressDialog.show()
                        if (fragment is StandbyFragment) {
                            //delay ~15 seconds
                            delay(10000L)
                        }
                        progressView.isIndeterminate = false
                        withContext(Dispatchers.IO) {
                            logs = LogReader.getLogEntries(
                                when {
                                    radio5Button.isChecked -> LOGS_LAST_5_MINUTES
                                    radio10Button.isChecked -> LOGS_LAST_10_MINUTES
                                    else -> null
                                }
                            ) { p ->
                                run {
                                    try {
                                        progressView.progress = p
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }.toMutableList()

                            logString = logs.joinToString("\n")

                            Log.d(TAG, "Log content size: ${logString.getSizeInMB()} MB")

                            // save file to internal storage
                            context.openFileOutput(
                                "${ContextCompat.getString(context, R.string.ceno_android_logs_file_name)}.txt", Context.MODE_PRIVATE)
                                .use {
                                    it.write(logString.toByteArray())
                                }

                            withContext(Dispatchers.Main) {

                                progressDialog.setOnDismissListener {  } // reset dismissListener

                                progressView.progress = 100
                                delay(200)

                                progressDialog.dismiss()

                                // prompt the user to view or share
                                viewAndShareLogs().show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun viewAndShareLogs(): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.ceno_log_file_saved))
            setMessage(context.getString(R.string.ceno_log_file_saved_desc))
            setNegativeButton(context.getString(R.string.share_logs)) { _, _ ->
                onDismiss.invoke()
                val directions = NavGraphDirections.actionGlobalShareFragment(
                    arrayOf(
                        ShareData(
                            url = "Logfile",
                            title = "Logs",
                        ),
                    )
                ).apply {
                    setLogsFilePath("${getString(context, R.string.ceno_android_logs_file_name)}.txt")
                }
                fragment.findNavController().navigate(directions)
            }
            setNeutralButton(context.getString(R.string.view_logs)) { _, _ ->
                fragment.findNavController().navigate(
                    if (fragment is StandbyFragment)
                        R.id.action_standbyFragment_to_androidLogFragment
                    else
                        R.id.action_settingsFragment_to_androidLogFragment,
                )
                onDismiss.invoke()
            }
            setPositiveButton(R.string.download_logs) { _, _ ->
                (fragment.requireActivity() as BrowserActivity).getLogfileLocation.launch(getString(context, R.string.ceno_android_logs_file_name))
            }
            create()
        }
    }

    private fun getProgressDialog(progressDialogView: View): AlertDialog {
        return AlertDialog.Builder(context)
            .setView(progressDialogView)
            .create()
            .apply {
                setOnDismissListener {
                    Toast.makeText(
                        context,
                        ContextCompat.getString(context, R.string.canceled),
                        Toast.LENGTH_LONG
                    ).show()
                    job?.cancel()
                    dismiss()
                }
                progressDialogView.findViewById<ImageButton>(R.id.cancel)
                    .setOnClickListener { dismiss() }
            }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}