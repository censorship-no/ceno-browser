/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.components.ceno

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.support.base.android.Padding
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.view.setPadding

/**
 * A [Toolbar.Action] implementation for CENO purge toolbar button.
 */
@Suppress("LongParameterList")
open class ClearToolbarAction(
    internal val padding: Padding? = null,
    private val listener: () -> Unit,
    private var context: Context
) : Toolbar.Action {

    private lateinit var clearImageView: ImageView
    override fun createView(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val rootView = inflater.inflate(R.layout.clear_toolbar_action, parent, false)

        rootView.setOnClickListener { listener.invoke() }

        val backgroundResource =
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless)

        rootView.setBackgroundResource(backgroundResource)
        padding?.let { rootView.setPadding(it) }

        clearImageView = rootView.findViewById(R.id.action_image)

        return rootView
    }

    override fun bind(view: View) {
        /* Nothing to bind */
    }

    fun updateIconColor(context: Context) {
        clearImageView.drawable.setTint(
            ContextCompat.getColor(
                context,
                context.theme.resolveAttribute(R.attr.iconPrimary)
            )
        )
    }
}
