package ie.equalit.ceno.settings.changeicon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.databinding.FragmentChangeIconBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.changeicon.appicons.*

class ChangeIconFragment : Fragment() {

    var _binding: FragmentChangeIconBinding? = null
    val binding get() = _binding!!

    var adapter: AppIconsAdapter? = null

    private var _appIconsInteractor: AppIconsInteractor? = null
    private val appIconsInteractor: AppIconsInteractor
        get() = _appIconsInteractor!!

    private var appIconsView: AppIconsView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))

        // Inflate the layout for this fragment
        _binding = FragmentChangeIconBinding.inflate(inflater, container, false);
        val activity = activity as BrowserActivity
        val appIconModifier = AppIconModifier(requireContext())

        _appIconsInteractor = AppIconsInteractor(
            controller = DefaultAppIconsController(
                activity = activity,
                appIconModifier = appIconModifier
            )
        )

        appIconsView = AppIconsView(
            binding.appIconsList,
            appIconsInteractor
        )

        // Check that app icon is setting matches the launcher enabled in the manifest
        for (icon in AppIcon.values()){
            if (appIconModifier.isEnabled(icon)){
                Settings.setAppIcon(requireContext(), icon.componentName)
            }
        }

        updateChangeIconView()

        return binding.root
    }

    private fun updateChangeIconView() {
        appIconsView?.update(requireContext())
    }

    companion object {
        private const val TAG = "ChangeIconFragment"
    }
}