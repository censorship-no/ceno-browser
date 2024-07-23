/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.databinding.LogTextItemBinding

class LogTextAdapter() :
    ListAdapter<String, LogTextAdapter.LogTextViewHolder>(StringItemDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LogTextViewHolder {
        val binding = LogTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogTextViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogTextViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal object StringItemDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem

        @Suppress("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem
    }

    class LogTextViewHolder(
        val binding: LogTextItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: String) {
            binding.logItem.text = log
        }
    }
}
