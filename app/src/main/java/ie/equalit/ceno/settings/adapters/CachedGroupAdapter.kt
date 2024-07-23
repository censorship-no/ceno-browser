/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import ie.equalit.ceno.databinding.ExpandableListChildItemBinding
import ie.equalit.ceno.databinding.ExpandableListGroupItemBinding


class CachedGroupAdapter(
    private val context: Context,
    private val groupList: List<GroupItem>,
    private val clickListener: GroupClickListener?
) :
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return groupList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groupList[groupPosition].children.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return groupList[groupPosition].children[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding =
            ExpandableListGroupItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.groupNameTextView.text = groupList[groupPosition].name
        return binding.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding =
            ExpandableListChildItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.childNameTextView.text = groupList[groupPosition].children[childPosition]
        binding.root.setOnClickListener {
            clickListener?.onLinkClicked(groupList[groupPosition].children[childPosition])
        }
        return binding.root
    }

    data class GroupItem(val name: String, val children: List<String>)

    interface GroupClickListener {
        fun onLinkClicked(url: String)
    }
}