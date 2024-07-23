package ie.equalit.ceno.settings

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.view.iterator
import ie.equalit.ceno.R
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.android.view.showKeyboard
import java.net.URLEncoder
import java.util.Locale
import java.util.regex.Pattern

class ExtraBTBootstrapsDialog(
    val context: Context,
    val btSourcesMap: MutableMap<String, String>,
    val updatePrefs: () -> Unit = {}
) {

    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        val customDialogView = View.inflate(context, R.layout.custom_extra_bt_dialog, null)
        val customBTSourcesView = customDialogView.findViewById<EditText>(R.id.bootstrap)

        val extraBtOptionsDialogView = View.inflate(context, R.layout.extra_bt_options_dialog, null)
        val linearLayout = extraBtOptionsDialogView.findViewById<LinearLayout>(R.id.linear_layout)
        val tvCustomSource = extraBtOptionsDialogView.findViewById<TextView>(R.id.tv_custom_sources)

        builder.apply {
            setTitle(getString(context, R.string.select_extra_bt_source))
            setView(extraBtOptionsDialogView)
            setNegativeButton(R.string.customize_addon_collection_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.customize_add_bootstrap_save) { _, _ ->
                val allSelectedIPs = mutableListOf<String>()
                for (child in linearLayout.iterator()) {
                    if (child is CheckBox && child.isChecked) {
                        allSelectedIPs.add(
                            btSourcesMap.entries.find { e ->
                                e.key.lowercase() == child.text.toString().trim().lowercase()
                            }?.value ?: child.text.toString().trim()
                        )
                    }
                }

                CenoSettings.ouinetClientRequest(
                    context,
                    OuinetKey.EXTRA_BOOTSTRAPS,
                    OuinetValue.OTHER,
                    URLEncoder.encode(allSelectedIPs.joinToString(" "), "UTF-8"),
                    object : OuinetResponseListener {
                        override fun onSuccess(message: String, data: Any?) {
                            CenoSettings.setExtraBitTorrentBootstrap(
                                context,
                                message.split("+").toTypedArray()
                            )
                            updatePrefs()
                            Toast.makeText(
                                context,
                                getString(context, R.string.ouinet_client_fetch_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onError() {
                            Toast.makeText(
                                context,
                                getString(context, R.string.ouinet_client_fetch_fail),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            btSourcesMap.forEach {
                linearLayout.addView(
                    CheckBox(context).apply {
                        text = Locale("", it.key).displayCountry
                        isChecked =
                            CenoSettings.getLocalBTSources(context)?.contains(it.value) == true
                        isAllCaps = false
                        setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.fx_mobile_text_color_primary
                            )
                        )
                    }
                )
            }
            // add custom sources
            CenoSettings.getLocalBTSources(context)?.forEach {
                it.let { source ->
                    if (source.trim().isNotEmpty() && !btSourcesMap.containsValue(source.trim())) {
                        linearLayout.addView(
                            CheckBox(context).apply {
                                text = it
                                isChecked = true
                                isAllCaps = false
                            }
                        )
                    }
                }
            }

            tvCustomSource.setOnClickListener {

                // This prevents the view from being added multiple times and causing a crash
//                (customDialogView.parent as? ViewGroup)?.removeView(customDialogView)

                val alertDialog2 = AlertDialog.Builder(context).apply {
                    setTitle(context.getString(R.string.customize_extra_bittorrent_bootstrap))
                    setView(customDialogView)
                    setNegativeButton(R.string.customize_addon_collection_cancel) { dialog: DialogInterface, _ ->
                        customBTSourcesView.hideKeyboard()
                        dialog.cancel()
                    }
                    setPositiveButton(R.string.customize_add_bootstrap_save) { _, _ ->
                        val ipAddresses = customBTSourcesView.text.toString().trim().split(",")

                        for (ipAddress in ipAddresses) {
                            // Pattern for validating IPs
                            val ipPattern = Pattern.compile(
                                """^(25[0-5]|2[0-4]\d|[01]?\d\d?)\.(25[0-5]|2[0-4]\d|[01]?\d\d?)\.(25[0-5]|2[0-4]\d|[01]?\d\d?)\.(25[0-5]|2[0-4]\d|[01]?\d\d?)$"""
                            )
                            if (!ipPattern.matcher(ipAddress.trim()).matches()) {
                                Toast.makeText(
                                    context,
                                    getString(context, R.string.bt_invalid_ip_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                customBTSourcesView.hideKeyboard()
                                return@setPositiveButton
                            }
                        }

                        // Add IPs to the list of IP sources
                        for (ip in ipAddresses) {
                            linearLayout.addView(
                                CheckBox(context).apply {
                                    text = ip
                                    isChecked = true
                                    isAllCaps = false
                                }
                            )
                        }

                        customBTSourcesView.hideKeyboard()
                    }
                    customBTSourcesView.requestFocus()
                    customBTSourcesView.showKeyboard()
                    create()
                }

                alertDialog2.show()
            }

        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}