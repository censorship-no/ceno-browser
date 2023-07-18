package ie.equalit.ceno.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_NOTIFICATION_PERMISSIONS
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentOnboardingBatteryBinding
import ie.equalit.ceno.ext.requireComponents
import mozilla.components.support.base.feature.ActivityResultHandler

/**
 * A simple [Fragment] subclass.
 * Use the [OnboardingBatteryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnboardingBatteryFragment : Fragment(), ActivityResultHandler {
    private var _binding: FragmentOnboardingBatteryBinding? = null
    private val binding get() = _binding!!

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBatteryBinding.inflate(inflater, container,false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /* This is Android 13 or later, ask for permission POST_NOTIFICATIONS */
            binding.tvOnboardingPermissionText.text = getString(R.string.onboarding_battery_text_v33)
            binding.button.setOnClickListener {
                allowPostNotifications()
            }
        }
        else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            binding.tvOnboardingPermissionText.text = getString(R.string.onboarding_battery_text)
            binding.button.setOnClickListener {
                disableBatteryOptimization()
            }
        }
    }

    private fun disableBatteryOptimization() {
        if (!requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())) {
            OnboardingFragment.transitionToHomeFragment(
                requireContext(),
                requireActivity(),
                sessionId
            )
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        super.onActivityResult(requestCode, resultCode, data)
        if (requireComponents.permissionHandler.onActivityResult(requestCode, data, resultCode)) {
            OnboardingThanksFragment.transitionToFragment(requireActivity(), sessionId)
        }
        else {
            (activity as BrowserActivity).updateView {
                OnboardingWarningFragment.transitionToFragment(requireActivity(), sessionId)
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allowPostNotifications() {
        if (!requireComponents.permissionHandler.requestPostNotificationsPermission(this)) {
            OnboardingThanksFragment.transitionToFragment(
                requireActivity(),
                sessionId
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSIONS) {
            requireComponents.ouinet.background.start()
            disableBatteryOptimization()
        }
        else {
            Log.e(TAG, "Unknown request code received: $requestCode")
            OnboardingThanksFragment.transitionToFragment(requireActivity(), sessionId)
        }
    }

    companion object {
        private const val SESSION_ID = "session_id"

        @JvmStatic
        protected fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }

        const val TAG = "ONBOARD_BATTERY"
        fun create(sessionId: String? = null) = OnboardingBatteryFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }

        fun transitionToFragment(activity: FragmentActivity, sessionId: String?) {
            activity.supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_back_in,
                    R.anim.slide_back_out
                )
                replace(R.id.container,
                    create(sessionId),
                    TAG
                )
                addToBackStack(null)
                commit()
            }
        }
    }
}