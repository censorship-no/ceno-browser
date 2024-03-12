package ie.equalit.ceno.onboarding

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentOnboardingBinding
import ie.equalit.ceno.ext.ceno.onboardingToHome
import ie.equalit.ceno.ext.requireComponents

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

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
        binding.btnOnboardingContinue.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_onboardingPublicPvtFragment)
        }

        binding.btnOnboardingSkip.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                /* Android 13 or later, always ask for permissions
                * go to home is permissions are granted */
                if (requireComponents.permissionHandler.isAllowingPostNotifications() &&
                    requireComponents.permissionHandler.isIgnoringBatteryOptimizations()
                ){
                    findNavController().onboardingToHome(requireComponents)
                } else {
                    findNavController().navigate(R.id.action_onboardingFragment_to_onboardingBatteryFragment)
                }
            } else {
                findNavController().onboardingToHome(requireComponents)
            }
        }
    }
}