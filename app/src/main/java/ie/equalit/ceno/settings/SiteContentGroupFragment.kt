/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.settings

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentSiteContentGroupBinding
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SiteContentGroupFragment : Fragment(), CachedGroupAdapter.GroupClickListener {

    private var _binding: FragmentSiteContentGroupBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSiteContentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_groups_count)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("groups")?.let { groups ->
            binding.groupListing.setAdapter(
                CachedGroupAdapter(
                    requireContext(),
                    convertToMap(
                        groups.trim()
                    ),
                    this
                )
            )
            binding.downloadButton.isGone = false
            binding.downloadButton.setOnClickListener {
                // confirmation nudge
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.confirm_groups_file_download))
                    setMessage(getString(R.string.confirm_groups_file_download_desc))
                    setNegativeButton(getString(R.string.ceno_clear_dialog_cancel)) { _, _ -> }
                    setPositiveButton(getString(R.string.onboarding_battery_button)) { _, _ ->
                        downloadGroups()
                    }
                    create()
                }.show()
            }
        }

    }

    private fun convertToMap(groups: String): List<CachedGroupAdapter.GroupItem> {

        val urls = groups.split("\n")

        val map = mutableMapOf<String, MutableList<String>>()

        for (url in urls) {
            val parts = url.split("/")
            val baseUrl = parts.first()
            val subUrls = mutableListOf<String>()
            parts.drop(1).forEach { subUrls.add("$baseUrl/$it") }
            if (subUrls.isEmpty()) subUrls.add(baseUrl)

            map[baseUrl] = if (map[baseUrl].isNullOrEmpty()) subUrls else map[baseUrl].apply {
                this!!.addAll(subUrls)
            }!!
        }

        val result = mutableListOf<CachedGroupAdapter.GroupItem>()
        map.keys.forEach { result.add(CachedGroupAdapter.GroupItem(it, map[it]!!.toList())) }

        return result
    }

    private fun downloadGroups() {
        var file: File?

        val progressDialogView = View.inflate(context, R.layout.progress_dialog, null)
        val progressView = progressDialogView.findViewById<ProgressBar>(R.id.progress_bar)

        val progressDialog = AlertDialog.Builder(requireContext())
            .setView(progressDialogView)
            .create()
            .apply {
                setOnDismissListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.canceled),
                        Toast.LENGTH_LONG
                    ).show()
                    job?.cancel()
                    dismiss()
                }
                progressDialogView.findViewById<ImageButton>(R.id.cancel)
                    .setOnClickListener { dismiss() }
            }

        progressDialog.show()

        job = viewLifecycleOwner.lifecycleScope.launch {

            withContext(Dispatchers.IO) {

                // save file to external storage
                file = File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path + "/${
                        getString(R.string.ceno_android_content_group_file_name)
                    }.txt"
                )
                file?.writeText(arguments?.getString("groups")!!)

                withContext(Dispatchers.Main) {

                    progressDialog.setOnDismissListener { } // reset dismissListener

                    progressView.progress = 100
                    delay(200)

                    progressDialog.dismiss()

                    // prompt the user to share or dismiss
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(context.getString(R.string.ceno_log_content_group_file_saved))
                        setPositiveButton(getString(R.string.share_logs)) { _, _ ->
                            if (file?.exists() == true) {
                                val uri = FileProvider.getUriForFile(
                                    requireContext(),
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file!!
                                )
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                intent.setType("*/*")
                                intent.putExtra(Intent.EXTRA_STREAM, uri)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent)
                            }
                        }
                        setNegativeButton(getString(R.string.dismiss)) { _, _ -> }
                        create()
                    }.show()
                }
            }
        }
    }

    override fun onLinkClicked(url: String) {
        (activity as BrowserActivity).openToBrowser(url = url, newTab = true)
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

}