package ie.equalit.ceno.onboarding

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ie.equalit.ceno.R
import ie.equalit.ceno.home.HomeFragment
import ie.equalit.ceno.databinding.FragmentOnboardingBinding
import ie.equalit.ceno.settings.Settings

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container,false)
        container?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ceno_onboarding_background))
        (activity as AppCompatActivity).supportActionBar!!.hide()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOnboardingStart.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_back_in,
                    R.anim.slide_back_out
                )
                replace(
                    R.id.container,
                    OnboardingPublicPvtFragment.create(sessionId),
                    OnboardingPublicPvtFragment.TAG
                )
                addToBackStack(null)
                commit()
            }
        }
        binding.btnOnboardingStartSkip.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                /* Android 13 or later, always ask for permissions */
                OnboardingBatteryFragment.transitionToFragment(requireActivity(), sessionId)
            }
            else {
                binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ceno_onboarding_background))
                transitionToHomeFragment(requireContext(), requireActivity(), sessionId)
            }
        }
    }

    companion object {
        private const val SESSION_ID = "session_id"

        @JvmStatic
        protected fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }

        const val TAG = "ONBOARD"
        fun create(sessionId: String? = null) = OnboardingFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }

        fun transitionToHomeFragment(context: Context, activity: FragmentActivity, sessionId: String?) {

            Settings.setShowOnboarding(context , false)

            activity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity.supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out,
                    R.anim.slide_back_in,
                    R.anim.fade_out
                )
                replace(
                    R.id.container,
                    HomeFragment.create(sessionId),
                    HomeFragment.TAG
                )
                commit()
            }
        }

    }
}