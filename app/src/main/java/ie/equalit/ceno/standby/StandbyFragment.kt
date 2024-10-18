package ie.equalit.ceno.standby

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_SETTINGS
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentStandbyBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.dialogs.ExtraBTBootstrapsDialog
import ie.equalit.ceno.settings.ExportAndroidLogsDialog
import ie.equalit.ceno.settings.Settings
import ie.equalit.ouinet.Ouinet.RunningState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.lib.state.ext.consumeFrom
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 */
class StandbyFragment : Fragment() {

    private val refreshIntervalMS: Long = 1000

    protected var isCenoStopping : Boolean? = false
        get() = arguments?.getBoolean(shutdownCeno)

    protected val doClear: Boolean?
        get() = arguments?.getBoolean(DO_CLEAR)

    private var currentStatus = RunningState.Starting

    private var index = 0

    private val displayText: List<Int> = listOf(
        R.string.standby_message_one,
        R.string.standby_message_one,
        R.string.standby_message_one,
        R.string.standby_message_one,
        R.string.standby_message_two,
        R.string.standby_message_two,
        R.string.standby_message_two,
        R.string.standby_message_two,
        R.string.standby_message_three,
        R.string.standby_message_three,
        R.string.standby_message_three,
        R.string.standby_message_three
    )

    private var displayTextStopping: MutableList<Int> = mutableListOf(
        R.string.shutdown_message_two,
        R.string.shutdown_message_two,
        R.string.shutdown_message_two,
    )

    private var extraInfoList: List<Int> = listOf(
        R.string.standby_tip_bridge,
        R.string.standby_tip_icon,
        R.string.standby_tip_announcements,
        R.string.standby_tip_pdf
    )

    private var dialog: AlertDialog? = null
    private var isAnyDialogVisible = false

    private var _binding : FragmentStandbyBinding? = null
    private val binding get() = _binding!!

    private fun isNetworkAvailable(): Boolean {
        val cm : ConnectivityManager = requireContext().getSystemService() ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cap = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networks: Array<Network> = cm.allNetworks
            for (n in networks) {
                val nInfo: NetworkInfo = cm.getNetworkInfo(n) ?: return false
                if (nInfo != null && nInfo.isConnected) return true
            }
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStandbyBinding.inflate(inflater, container, false)
        container?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ceno_standby_background))
        (activity as AppCompatActivity).supportActionBar!!.hide()
        index = 0
        repeat(3){
            displayTextStopping.add (0,
                if (doClear == true) {
                    R.string.shutdown_message_one
                } else {
                    R.string.shutdown_message_two
                }
            )
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var infoIndex = (System.currentTimeMillis() % 4).toInt()
        if (isCenoStopping == true) {
            binding.llStandbyExtraInfo.visibility = View.GONE
            lifecycleScope.launch {
                while (index < displayTextStopping.size) {
                    binding.tvStatus.text =
                        ContextCompat.getString(requireContext(), displayTextStopping[index])
                    index += 1
                    delay(refreshIntervalMS)
                }
            }
        } else {
            binding.root.consumeFrom(requireComponents.appStore, viewLifecycleOwner) {
                if (getView() == null)
                    return@consumeFrom
                currentStatus = it.ouinetStatus
                context?.let { ctx ->
                    updateDisplayText(ctx, infoIndex)
                }
                if (currentStatus == RunningState.Stopped) {
                    if(isCenoStopping == false) {
                        tryAgain()
                    }
                }
            }
        }
    }

    private fun displayTimeoutDialog(ctx: Context) {
        binding.progressBar.visibility = View.INVISIBLE
        val timeoutDialogBuilder = AlertDialog.Builder(requireContext())
        val timeoutDialogView = View.inflate(requireContext(), R.layout.layout_standby_timeout, null)

        timeoutDialogBuilder.apply {
            setView(timeoutDialogView)
        }

        timeoutDialogBuilder.setOnCancelListener{
            isAnyDialogVisible = false
            tryAgain()
        }

        dialog = timeoutDialogBuilder.create()

        val btnTryAgain = timeoutDialogView.findViewById<Button>(R.id.btn_try_again)
        btnTryAgain?.setOnClickListener {
            dialog?.dismiss()
            isAnyDialogVisible = false
            tryAgain()
        }
        val btnNetwork = timeoutDialogView.findViewById<Button>(R.id.btn_network_settings)
        btnNetwork?.setOnClickListener {
            dialog?.dismiss()
            try {
                startActivity(Intent(ACTION_WIRELESS_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                e.message?.let { it1 -> Log.w("ERROR", it1) }
                startActivity(Intent(ACTION_SETTINGS))
            }
        }
        val btnExtraBTBootstraps = timeoutDialogView.findViewById<Button>(R.id.btn_extra_bt_bootstraps)
        btnExtraBTBootstraps.setOnClickListener{
            dialog?.dismiss()
            val btSourcesMap = mutableMapOf<String, String>()
            for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) btSourcesMap[Locale("", entry[0]).displayCountry] = entry[1]
            val extraBTDialog = ExtraBTBootstrapsDialog(requireContext(), btSourcesMap).getDialog()
            extraBTDialog.setOnDismissListener {
                tryAgain()
                isAnyDialogVisible = false
            }
            extraBTDialog.show()
            isAnyDialogVisible = true
        }

        val btnExportLogs = timeoutDialogView.findViewById<Button>(R.id.btn_export_logs)
        btnExportLogs.setOnClickListener {
            dialog?.dismiss()
            val exportLogsDialog = ExportAndroidLogsDialog(requireContext(), this) {
                isAnyDialogVisible = false
            }.getDialog()
            exportLogsDialog.setOnCancelListener {
                tryAgain()
                isAnyDialogVisible = false
            }
            exportLogsDialog.show()
            isAnyDialogVisible = true
        }
        val btnTakeMeAnyway = timeoutDialogView.findViewById<Button>(R.id.btn_take_me_anyway)
        btnTakeMeAnyway?.setOnClickListener {
            navigateToBrowser()
        }
        val dontShowAgain = timeoutDialogView.findViewById<CheckBox>(R.id.chk_dont_show_again)
        dontShowAgain.setOnCheckedChangeListener { _, isChecked ->
            Settings.setShowStandbyWarning(ctx, !isChecked)
        }
        dialog?.show()
        isAnyDialogVisible = true
    }

    private fun tryAgain() {
        //restart progressbar indicator
        view?.let {
            binding.progressBar.visibility = View.VISIBLE
            index = 0
            context?.let { ctx ->
                updateDisplayText(ctx, (System.currentTimeMillis() % 2).toInt())
            }
        }
    }

    private fun navigateToBrowser() {
        dialog?.dismiss()
        findNavController().popBackStack(R.id.standbyFragment, true)
        // go to home or browser
        if (requireComponents.core.store.state.selectedTab == null)
            findNavController().navigate(R.id.action_global_home)
        else
            findNavController().navigate((R.id.action_global_browser))
    }

    private fun updateDisplayText(ctx: Context, infoIndex:Int) {
        viewLifecycleOwner.lifecycleScope.launch{
            Log.d("StandbyFragment", "Update display text")
            while (currentStatus == RunningState.Starting || currentStatus == RunningState.Degraded) {
                if(!isAnyDialogVisible) {
                    if (isNetworkAvailable()) {
                        binding.ivExtraInfo.setImageDrawable(ContextCompat
                        .getDrawable(requireContext(), R.drawable.lightbulb_icon))
                        binding.ivExtraInfo.drawable.setTint(ContextCompat.getColor(requireContext(), R.color.ceno_standby_logo_color))
                        binding.tvExtraInfoTitle.visibility = View.VISIBLE
                        //randomly select text
                        binding.tvExtraInfoText.text = getString(extraInfoList[infoIndex])
                    } else {
                        binding.ivExtraInfo.setImageDrawable(ContextCompat
                        .getDrawable(requireContext(), R.drawable.ic_no_internet))
                        binding.ivExtraInfo.drawable.setTint(ContextCompat.getColor(requireContext(), R.color.fx_mobile_icon_color_warning))
                        binding.tvExtraInfoTitle.visibility = View.GONE
                        binding.tvExtraInfoText.text = getString(R.string.standby_no_internet_text)
                }
                    if (index < displayText.size) {
                        binding.tvStatus.text = getString(displayText[index])
                        index += 1
                    } else {
                        if (Settings.shouldShowStandbyWarning(ctx)) {
                            displayTimeoutDialog(ctx)
                        }
                        else {
                            navigateToBrowser()
                        }
                        break
                    }
                }
                delay(refreshIntervalMS)
            }
            Log.d("StandbyFragment", "Current setting of standby warning: ${Settings.shouldShowStandbyWarning(ctx)}")
            if (currentStatus == RunningState.Started) {
                //Navigate away
                navigateToBrowser()
            }
            if (currentStatus == RunningState.Stopping) {
                if (isCenoStopping == true) {
                    binding.tvStatus.text = getString(R.string.shutdown_message_two)
                    binding.llStandbyExtraInfo.visibility = View.INVISIBLE
                } else {
                    binding.tvStatus.text = getString(R.string.standby_restarting_text)
                    binding.llStandbyExtraInfo.visibility = View.INVISIBLE
                }
            }
            cancel()
        }
    }

    companion object {
        const val shutdownCeno = "shutdownCeno"
        const val DO_CLEAR = "do_clear"
    }

    override fun onDetach() {
        super.onDetach()
        dialog?.dismiss()
    }
}