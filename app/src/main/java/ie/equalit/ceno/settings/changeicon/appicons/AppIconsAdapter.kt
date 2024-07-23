/*
 * Copyright (c) 2023 DuckDuckGo, eQualitie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ie.equalit.ceno.settings.changeicon.appicons

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.databinding.ItemAppIconBinding

class AppIconsAdapter(private val interactor: AppIconsInteractor) :
    ListAdapter<AppIcon, AppIconsAdapter.IconViewHolder>(
        AppIconsDiffCallback
    ) {

    private val iconViewData: MutableList<AppIcon> = enumValues<AppIcon>().toMutableList()
    private var selectedIcon: String? = null

    class IconViewHolder(
        val binding: ItemAppIconBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = iconViewData.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): IconViewHolder {
        val binding = ItemAppIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: IconViewHolder,
        position: Int
    ) {
        val viewElement = iconViewData[position]
        holder.itemView.setOnClickListener { interactor.onSelectAppIcon(viewElement) }
        holder.binding.icon.setBackgroundResource(viewElement.icon)
        if (viewElement.componentName == selectedIcon)
            holder.itemView.isSelected = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyChanges(selected: String?) {
        selectedIcon = selected
        notifyDataSetChanged()
    }

    internal object AppIconsDiffCallback : DiffUtil.ItemCallback<AppIcon>() {
        override fun areItemsTheSame(oldItem: AppIcon, newItem: AppIcon) =
            oldItem.componentName == newItem.componentName

        override fun areContentsTheSame(oldItem: AppIcon, newItem: AppIcon) =
            oldItem.icon == newItem.icon && oldItem.componentName == newItem.componentName

        override fun getChangePayload(oldItem: AppIcon, newItem: AppIcon): Any? {
            return if (oldItem.icon == newItem.icon && oldItem.componentName == newItem.componentName) {
                newItem
            } else null
        }
    }

}
