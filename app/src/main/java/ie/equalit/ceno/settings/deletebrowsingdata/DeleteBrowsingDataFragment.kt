/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.deletebrowsingdata

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.lib.state.ext.flowScoped
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentDeleteBrowsingDataBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.Settings

@SuppressWarnings("TooManyFunctions", "LargeClass")
class DeleteBrowsingDataFragment : Fragment(R.layout.fragment_delete_browsing_data) {

    private lateinit var controller: DeleteBrowsingDataController
    private var scope: CoroutineScope? = null
    //private lateinit var settings: Settings

    private var _binding: FragmentDeleteBrowsingDataBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))

        val tabsUseCases = requireComponents.useCases.tabsUseCases
        //val downloadUseCases = requireComponents.useCases.downloadUseCases

        _binding = FragmentDeleteBrowsingDataBinding.bind(view)
        controller = DefaultDeleteBrowsingDataController(
            tabsUseCases.removeAllTabs,
            //downloadUseCases.removeAllDownloads,
            requireComponents.core.historyStorage,
            //requireComponents.core.permissionStorage,
            requireComponents.core.store,
            requireComponents.core.icons,
            requireComponents.core.engine,
        )
        //settings = requireContext().settings()

        getCheckboxes().iterator().forEach {
            it.onCheckListener = { _ ->
                updateDeleteButton()
                updatePreference(it)
            }
        }

        getCheckboxes().iterator().forEach {
            it.isChecked = when (it.id) {
                R.id.open_tabs_item -> Settings.deleteOpenTabs(requireContext())
                R.id.browsing_data_item -> Settings.deleteBrowsingHistory(requireContext())
                R.id.cookies_item -> Settings.deleteCookies(requireContext())
                R.id.cached_files_item -> Settings.deleteCache(requireContext())
                R.id.site_permissions_item -> Settings.deleteSitePermissions(requireContext())
                //R.id.downloads_item -> settings.deleteDownloads
                else -> true
            }
        }

        binding.deleteData.setOnClickListener {
            askToDelete()
        }
        updateDeleteButton()
    }

    private fun updatePreference(it: DeleteBrowsingDataItem) {
        when (it.id) {
            R.id.open_tabs_item -> Settings.setDeleteOpenTabs(requireContext(), it.isChecked)
            R.id.browsing_data_item -> Settings.setDeleteBrowsingHistory(requireContext(), it.isChecked)
            R.id.cookies_item -> Settings.setDeleteCookies(requireContext(), it.isChecked)
            R.id.cached_files_item -> Settings.setDeleteCache(requireContext(), it.isChecked)
            R.id.site_permissions_item -> Settings.setDeleteSitePermissions(requireContext(), it.isChecked)
            //R.id.downloads_item -> settings.deleteDownloads = it.isChecked
            else -> return
        }
    }

    override fun onStart() {
        super.onStart()

        scope = requireComponents.core.store.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { state -> state.tabs.size }
                .distinctUntilChanged()
                .collect { openTabs -> updateTabCount(openTabs) }
        }
    }

    override fun onResume() {
        super.onResume()
        //showToolbar(getString(R.string.preferences_delete_browsing_data))

        getCheckboxes().iterator().forEach {
            it.visibility = View.VISIBLE
        }

        updateItemCounts()
    }

    private fun updateDeleteButton() {
        val enabled = getCheckboxes().any { it.isChecked }

        binding.deleteData.isEnabled = enabled
        binding.deleteData.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    private fun askToDelete() {
        context?.let {
            AlertDialog.Builder(it).apply {
                setMessage(
                    it.getString(
                        R.string.delete_browsing_data_prompt_message_3,
                    ),
                )

                setNegativeButton(R.string.delete_browsing_data_prompt_cancel) { it: DialogInterface, _ ->
                    it.cancel()
                }

                setPositiveButton(R.string.delete_browsing_data_prompt_allow) { it: DialogInterface, _ ->
                    it.dismiss()
                    deleteSelected()
                }
                create()
            }.show()
        }
    }

    private fun deleteSelected() {
        startDeletion()
        lifecycleScope.launch(IO) {
            getCheckboxes().mapIndexed { i, v ->
                if (v.isChecked) {
                    when (i) {
                        OPEN_TABS_INDEX -> controller.deleteTabs()
                        HISTORY_INDEX -> controller.deleteBrowsingData()
                        COOKIES_INDEX -> controller.deleteCookies()
                        CACHED_INDEX -> controller.deleteCachedFiles()
                        PERMS_INDEX -> controller.deleteSitePermissions()
                        DOWNLOADS_INDEX -> controller.deleteDownloads()
                    }
                }
            }

            withContext(Main) {
                finishDeletion()
            }
        }
    }

    private fun startDeletion() {
        binding.progressBar.visibility = View.VISIBLE
        binding.deleteBrowsingDataWrapper.isEnabled = false
        binding.deleteBrowsingDataWrapper.isClickable = false
        binding.deleteBrowsingDataWrapper.alpha = DISABLED_ALPHA
        Toast.makeText(context, resources.getString(R.string.deleting_browsing_data_in_progress), Toast.LENGTH_SHORT).show()
    }

    private fun finishDeletion() {
        val popAfter = binding.openTabsItem.isChecked
        binding.progressBar.visibility = View.GONE
        binding.deleteBrowsingDataWrapper.isEnabled = true
        binding.deleteBrowsingDataWrapper.isClickable = true
        binding.deleteBrowsingDataWrapper.alpha = ENABLED_ALPHA

        updateItemCounts()

        Toast.makeText(context, resources.getString(R.string.preferences_delete_browsing_data_snackbar), Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        binding.progressBar.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        scope?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateItemCounts() {
        updateTabCount()
        updateHistoryCount()
        updateCookies()
        updateCachedImagesAndFiles()
        updateSitePermissions()
    }

    private fun updateTabCount(openTabs: Int = requireComponents.core.store.state.tabs.size) {
        binding.openTabsItem.apply {
            subtitleView.text = resources.getQuantityString(
                R.plurals.preferences_delete_browsing_data_tabs_subtitle,
                openTabs,
                openTabs
            )
            subtitleView.visibility = View.VISIBLE
        }
    }

    private fun updateHistoryCount() {

        viewLifecycleOwner.lifecycleScope.launch(IO) {
            val historyCount = requireComponents.core.historyStorage.getVisited().size
            launch(Main) {
                binding.browsingDataItem.apply {
                    subtitleView.text =
                        resources.getQuantityString(
                            R.plurals.preferences_delete_browsing_data_browsing_data_subtitle,
                            historyCount,
                            historyCount
                        )
                    subtitleView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateCookies() {
        // NO OP until we have GeckoView methods to count cookies
    }

    private fun updateCachedImagesAndFiles() {
        // NO OP until we have GeckoView methods to count cached images and files
    }

    private fun updateSitePermissions() {
        // NO OP until we have GeckoView methods for cookies and cached files, for consistency
    }

    private fun getCheckboxes(): List<DeleteBrowsingDataItem> {
        return listOf(
            binding.openTabsItem,
            binding.browsingDataItem,
            binding.cookiesItem,
            binding.cachedFilesItem,
            binding.sitePermissionsItem,
            //binding.downloadsItem,
        )
    }

    companion object {
        private const val TAG = "DeleteBrowsingDataFragment"
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.6f

        private const val OPEN_TABS_INDEX = 0
        private const val HISTORY_INDEX = 1
        private const val COOKIES_INDEX = 2
        private const val CACHED_INDEX = 3
        private const val PERMS_INDEX = 4
        private const val DOWNLOADS_INDEX = 5
    }
}
