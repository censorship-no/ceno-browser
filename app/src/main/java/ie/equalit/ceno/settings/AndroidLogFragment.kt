/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentAndroidLogBinding
import ie.equalit.ceno.settings.adapters.LogTextAdapter

class AndroidLogFragment : Fragment() {

    private var _binding: FragmentAndroidLogBinding? = null
    private val binding get() = _binding!!
    private val itemsPerBatch = 100
    private var loadedItemCount = 0
    private lateinit var allItems: List<String>
    private var adapter = LogTextAdapter()
    private var isLoading = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAndroidLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getActionBar().apply {
            show()
            title = getString(R.string.ceno_android_logs_file_name)
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

        // get logs from arguments
        allItems = arguments?.getStringArrayList(SettingsFragment.LOG) ?: emptyList()
        Log.d(TAG, "${allItems.size} logs retrieved")

        // Set adapter and load initial data
        binding.logRecyclerView.adapter = adapter
        loadInitialData()

        // load more items when a user scrolls to the bottom of the recyclerView
        binding.logRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (!isLoading
                    && loadedItemCount < allItems.size
                    && (layoutManager.childCount + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.itemCount
                ) {
                    loadMoreData()
                }
            }
        })
    }

    private fun loadInitialData() {

        Log.d(
            TAG,
            "Loading first ${if (allItems.size < itemsPerBatch) allItems.size else itemsPerBatch} logs"
        )

        // Load the initial set of data
        val initialData = loadBatchOfItems(0)
        loadedItemCount += initialData.size

        // Notify the adapter about the new data
        adapter.submitList(initialData)

        if (loadedItemCount > 0) {
            // recyclerview can now trigger loadMore when it's scrolled to the bottom
            isLoading = false
        } else {
            // show empty state view
            binding.tvEmptyStateText.isGone = false
            binding.logRecyclerView.isGone = true
        }
    }

    private fun loadMoreData() {

        Log.d(TAG, "User scrolled to bottom, loading more logs...")

        // Load the next batch of data
        val nextBatch = loadBatchOfItems(loadedItemCount)
        loadedItemCount += nextBatch.size

        // Notify the adapter about the new data
        adapter.submitList(nextBatch)
    }

    private fun loadBatchOfItems(startIndex: Int): List<String> {
        val endIndex = startIndex + itemsPerBatch
        return allItems.subList(0, endIndex.coerceAtMost(allItems.size))
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "AndroidLogFragment"
    }

}