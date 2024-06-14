package ie.equalit.ceno.standby

import android.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentStandbyBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.ExtraBTBootstrapsDialog
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

    private var dialog: AlertDialog? = null

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
        if (isCenoStopping == true) {
            binding.llNoInternet.visibility = View.GONE
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
                updateDisplayText()
                if (currentStatus == RunningState.Stopped) {
                    if(isCenoStopping == false) {
                        tryAgain()
                    }
                }
            }
        }
    }

    private fun displayTimeoutDialog() {

        val timeoutDialogBuilder = AlertDialog.Builder(requireContext())
        val timeoutDialogView = View.inflate(requireContext(), R.layout.layout_standby_timeout, null)

        timeoutDialogBuilder.apply {
            setView(timeoutDialogView)
            setPositiveButton(getString(R.string.standby_try_again)) { _, _ ->
                tryAgain()
            }
        }

        timeoutDialogBuilder.setOnCancelListener{
            tryAgain()
        }

        dialog = timeoutDialogBuilder.create()

        val btnNetwork = timeoutDialogView.findViewById<Button>(R.id.btn_network_settings)
        btnNetwork?.setOnClickListener {
            dialog?.dismiss()
            val intent = Intent(ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }
        val btnExtraBTBootstraps = timeoutDialogView.findViewById<Button>(R.id.btn_extra_bt_bootstraps)
        btnExtraBTBootstraps.setOnClickListener{
            dialog?.dismiss()
            val btSourcesMap = mutableMapOf<String, String>()
            for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) btSourcesMap[Locale("", entry[0]).displayCountry] = entry[1]
            val extraBTDialog = ExtraBTBootstrapsDialog(requireContext(), btSourcesMap).getDialog()
            extraBTDialog.setOnDismissListener {
                tryAgain()
            }
            extraBTDialog.show()

        }
        dialog?.show()

    }

    private fun tryAgain() {
        //restart progressbar indicator
        view?.let {
            binding.progressBar.isActivated = true
            index = 0
            updateDisplayText()
        }
    }

    private fun updateDisplayText() {
        viewLifecycleOwner.lifecycleScope.launch{
            while (currentStatus == RunningState.Starting) {
                if (isNetworkAvailable()) {
                    binding.llNoInternet.visibility = View.INVISIBLE
                } else
                    binding.llNoInternet.visibility = View.VISIBLE
                if (index < displayText.size)
                {
                    binding.tvStatus.text = getString(displayText[index])
                    index += 1
                } else {
                    displayTimeoutDialog()
                    break
                }
                delay(refreshIntervalMS)
            }
            if (currentStatus == RunningState.Started) {
                //Navigate away
                //go to home or browser
                dialog?.dismiss()
                findNavController().popBackStack(R.id.standbyFragment, true)
                if (requireComponents.core.store.state.selectedTab == null)
                    findNavController().navigate(R.id.action_global_home)
                else
                    findNavController().navigate((R.id.action_global_browser))
            }
            if (currentStatus == RunningState.Stopping) {
                if (isCenoStopping == true) {
                    binding.tvStatus.text = getString(R.string.shutdown_message_two)
                    binding.llNoInternet.visibility = View.INVISIBLE
                } else {
                    binding.tvStatus.text = getString(R.string.standby_restarting_text)
                    binding.llNoInternet.visibility = View.INVISIBLE
                }
            }
            cancel()
        }
    }

    companion object {
        const val shutdownCeno = "shutdownCeno"
        const val DO_CLEAR = "do_clear"
    }

}