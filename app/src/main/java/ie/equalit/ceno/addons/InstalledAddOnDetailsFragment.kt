/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.addons

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManagerException
import mozilla.components.feature.addons.ui.translateName

/**
 * A fragment to show the details of a installed add-on.
 */

class InstalledAddOnDetailsFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_installed_add_on_details, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        val addon = requireNotNull(arguments?.getParcelable<Addon>("add_on")).also {
            bindUI(it, rootView)
        }
        bindAddon(addon, rootView)
    }

    private fun bindAddon(addon: Addon, rootView: View) {
        scope.launch {
            try {
                val addons = activity?.baseContext?.components?.core?.addonManager?.getAddons()
                scope.launch(Dispatchers.Main) {
                    addons?.find { addon.id == it.id }.let {
                        if (it == null) {
                            throw AddonManagerException(Exception("Addon ${addon.id} not found"))
                        } else {
                            bindUI(it, rootView)
                        }
                    }
                }
            } catch (e: AddonManagerException) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        activity?.baseContext,
                        R.string.mozac_feature_addons_failed_to_query_add_ons,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun bindUI(addon: Addon, rootView: View) {
        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            title = addon.translateName(requireContext())
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

        bindEnableSwitch(addon, rootView)

        bindSettings(addon, rootView)

        bindDetails(addon, rootView)

        bindPermissions(addon, rootView)

        bindAllowInPrivateBrowsingSwitch(addon, rootView)

        bindRemoveButton(addon, rootView)
    }

    private fun bindVersion(addon: Addon, rootView: View) {
        val versionView = rootView.findViewById<TextView>(R.id.version_text)
        versionView.text = addon.version
    }

    private fun bindEnableSwitch(addon: Addon, rootView: View) {
        val switch = rootView.findViewById<SwitchCompat>(R.id.enable_switch)
        switch.setState(addon.isEnabled())
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activity?.components?.core?.addonManager?.enableAddon(
                    addon,
                    onSuccess = {
                        switch.setState(true)
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.mozac_feature_addons_successfully_enabled,
                                addon.translateName(requireContext())
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.mozac_feature_addons_failed_to_enable,
                                addon.translateName(requireContext())
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            } else {
                activity?.components?.core?.addonManager?.disableAddon(
                    addon,
                    onSuccess = {
                        switch.setState(false)
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.mozac_feature_addons_successfully_disabled,
                                addon.translateName(requireContext())
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.mozac_feature_addons_failed_to_disable,
                                addon.translateName(requireContext())
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }

    private fun bindSettings(addon: Addon, rootView: View) {
        val view = rootView.findViewById<View>(R.id.settings)
        view.isVisible = shouldSettingsBeVisible(addon)
        view.isEnabled = shouldSettingsBeVisible(addon)
        view.setOnClickListener {
            val intent = Intent(requireContext(), AddonSettingsActivity::class.java)
            intent.putExtra("add_on", addon)
            this.startActivity(intent)
        }
    }

    private fun bindDetails(addon: Addon, rootView: View) {
        rootView.findViewById<View>(R.id.details).setOnClickListener {
            findNavController().navigate(
                R.id.action_installedAddonDetailsFragment_to_addonDetailsFragment,
                bundleOf(
                    "add_on" to addon
                )
            )
        }
    }

    private fun bindPermissions(addon: Addon, rootView: View) {
        rootView.findViewById<View>(R.id.permissions).setOnClickListener {
            val intent = Intent(requireContext(), PermissionsDetailsActivity::class.java)
            intent.putExtra("add_on", addon)
            this.startActivity(intent)
        }
    }

    private fun bindAllowInPrivateBrowsingSwitch(addon: Addon, rootView: View) {
        val switch = rootView.findViewById<SwitchCompat>(R.id.allow_in_private_browsing_switch)
        switch.isChecked = addon.isAllowedInPrivateBrowsing()
        switch.setOnCheckedChangeListener { _, isChecked ->
            activity?.components?.core?.addonManager?.setAddonAllowedInPrivateBrowsing(
                addon,
                isChecked,
                onSuccess = {
                    switch.isChecked = isChecked
                },
            )
        }
    }

    private fun bindRemoveButton(addon: Addon, rootView: View) {
        rootView.findViewById<View>(R.id.remove_add_on).setOnClickListener {
            activity?.components?.core?.addonManager?.uninstallAddon(
                addon,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        getString(
                            R.string.mozac_feature_addons_successfully_uninstalled,
                            addon.translateName(requireContext())
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                    findNavController().popBackStack()
                },
                onError = { _, _ ->
                    Toast.makeText(
                        requireContext(),
                        getString(
                            R.string.mozac_feature_addons_failed_to_uninstall,
                            addon.translateName(requireContext())
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    private fun SwitchCompat.setState(checked: Boolean) {
        val text = if (checked) {
            R.string.mozac_feature_addons_enabled
        } else {
            R.string.mozac_feature_addons_disabled_section
        }
        setText(text)
        isChecked = checked
    }

    private fun shouldSettingsBeVisible(addon: Addon) =
        !addon.installedState?.optionsPageUrl.isNullOrEmpty()

}