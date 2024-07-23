package ie.equalit.ceno.share

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ie.equalit.ceno.databinding.ShareToAppsBinding

/**
 * Callbacks for possible user interactions on the [ShareCloseView]
 */
interface ShareToAppsInteractor {
    fun onShareToApp(appToShareTo: AppShareOption)
}

class ShareToAppsView(
    containerView: ViewGroup,
    interactor: ShareToAppsInteractor,
) {
    private val adapter = AppShareAdapter(interactor)
    private val recentAdapter = AppShareAdapter(interactor)
    private var binding: ShareToAppsBinding = ShareToAppsBinding.inflate(
        LayoutInflater.from(containerView.context),
        containerView,
        true,
    )

    init {
        binding.appsList.adapter = adapter
        binding.recentAppsList.adapter = recentAdapter
    }

    fun setShareTargets(targets: List<AppShareOption>) {
        binding.progressBar.visibility = View.GONE

        binding.appsList.visibility = View.VISIBLE
        adapter.submitList(targets)
    }

    fun setRecentShareTargets(recentTargets: List<AppShareOption>) {
        if (recentTargets.isEmpty()) {
            binding.recentAppsContainer.visibility = View.GONE
            return
        }
        binding.progressBar.visibility = View.GONE

        binding.recentAppsContainer.visibility = View.VISIBLE
        recentAdapter.submitList(recentTargets)
    }
}