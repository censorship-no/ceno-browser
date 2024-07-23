/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.addons

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManagerException
import mozilla.components.feature.addons.ui.AddonInstallationDialogFragment
import mozilla.components.feature.addons.ui.AddonsManagerAdapter
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.PermissionsDialogFragment
import mozilla.components.feature.addons.ui.translateName

/**
 * Fragment use for managing add-ons.
 */
class AddonsFragment : Fragment(), AddonsManagerAdapterDelegate {
    private lateinit var recyclerView: RecyclerView
    private val scope = CoroutineScope(Dispatchers.IO)

    private val addonProgressOverlay: View
        get() = requireView().findViewById(R.id.addonProgressOverlay)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_add_ons, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        bindRecyclerView(rootView)
    }

    override fun onStart() {
        super.onStart()

        this@AddonsFragment.view?.let { view ->
            bindRecyclerView(view)
        }

        addonProgressOverlay.visibility = View.GONE

        findPreviousDialogFragment()?.let { dialog ->
            dialog.onPositiveButtonClicked = onPositiveButtonClicked
        }
    }

    private fun bindRecyclerView(rootView: View) {
        recyclerView = rootView.findViewById(R.id.add_ons_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val placeholderAdapter = AddonPlaceholderAdapter().apply {
            submitList(listOf(AdapterItem.AddonPlaceholderItem))
        }
        recyclerView.adapter = placeholderAdapter
        scope.launch {
            try {
                val addons = requireContext().components.core.addonManager.getAddons()

                scope.launch(Dispatchers.Main) {
                    try {
                        val adapter = AddonsManagerAdapter(
                            this@AddonsFragment,
                            addons,
                            store = requireContext().components.core.store,
                        )
                        recyclerView.adapter = adapter
                    } catch (e: AddonManagerException) {
                        Log.d(TAG, "Failed to get AddonsManagerAdapter $e")
                    }
                }
            } catch (e: AddonManagerException) {
                Log.d(TAG, "Failed to query addons: $e")
            }
        }
    }

    override fun onAddonItemClicked(addon: Addon) {
        if (addon.isInstalled()) {
            findNavController().navigate(
                R.id.action_addonsFragment_to_installedAddonDetailsFragment,
                bundleOf(
                    "add_on" to addon
                )
            )
        } else {
            findNavController().navigate(
                R.id.action_addonsFragment_to_addonDetailsFragment,
                bundleOf(
                    "add_on" to addon
                )
            )
        }
    }

    override fun onInstallAddonButtonClicked(addon: Addon) {
        showPermissionDialog(addon)
    }

    private fun isAlreadyADialogCreated(): Boolean {
        return findPreviousDialogFragment() != null
    }

    private fun findPreviousDialogFragment(): PermissionsDialogFragment? {
        return parentFragmentManager.findFragmentByTag(PERMISSIONS_DIALOG_FRAGMENT_TAG) as? PermissionsDialogFragment
    }

    private fun showPermissionDialog(addon: Addon) {
        if (isInstallationInProgress) {
            return
        }

        val dialog = PermissionsDialogFragment.newInstance(
            addon = addon,
            permissions = emptyList(), // TODO: which permissions?
            onPositiveButtonClicked = onPositiveButtonClicked,
        )

        if (!isAlreadyADialogCreated()) {
            dialog.show(parentFragmentManager, PERMISSIONS_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showInstallationDialog(addon: Addon) {
        if (isInstallationInProgress) {
            return
        }

        val dialog = AddonInstallationDialogFragment.newInstance(
            addon = addon,
            onConfirmButtonClicked = { _, allowInPrivateBrowsing ->
                if (allowInPrivateBrowsing) {
                    requireContext().components.core.addonManager.setAddonAllowedInPrivateBrowsing(
                        addon,
                        allowInPrivateBrowsing,
                    )
                }
            },
        )

        if (!isAlreadyADialogCreated()) {
            dialog.show(parentFragmentManager, INSTALLATION_DIALOG_FRAGMENT_TAG)
        }
    }

    private val onPositiveButtonClicked: ((Addon) -> Unit) = { addon ->
        addonProgressOverlay.visibility = View.VISIBLE
        isInstallationInProgress = true
        requireContext().components.core.addonManager.installAddon(
            url = addon.downloadUrl,
            onSuccess = {
                runIfFragmentIsAttached {
                    isInstallationInProgress = false
                    this@AddonsFragment.view?.let { view ->
                        bindRecyclerView(view)
                        showInstallationDialog(it)
                    }

                    addonProgressOverlay.visibility = View.GONE
                }
            },
            onError = { _ ->
                runIfFragmentIsAttached {
                    context?.let {
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.mozac_feature_addons_failed_to_install,
                                addon.translateName(it)
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    addonProgressOverlay.visibility = View.GONE
                    isInstallationInProgress = false
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.preferences_add_ons)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ceno_action_bar
                    )
                )
            )
        }
    }

    /**
     * Whether or not an add-on installation is in progress.
     */
    private var isInstallationInProgress = false

    companion object {
        private const val TAG = "AddonsFragment"
        private const val PERMISSIONS_DIALOG_FRAGMENT_TAG = "ADDONS_PERMISSIONS_DIALOG_FRAGMENT"
        private const val INSTALLATION_DIALOG_FRAGMENT_TAG = "ADDONS_INSTALLATION_DIALOG_FRAGMENT"
    }
}
