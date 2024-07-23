/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.addons

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateDescription
import mozilla.components.feature.addons.ui.translateName
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A fragment to show the details of an add-on.
 */
class AddonDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_addon_details, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        val addon = requireNotNull(arguments?.getParcelable<Addon>("add_on"))
        bind(addon, rootView)
    }

    private fun bind(addon: Addon, rootView: View) {
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

        bindDetails(addon, rootView)

        bindAuthor(addon, rootView)

        bindVersion(addon, rootView)

        bindLastUpdated(addon, rootView)

        bindWebsite(addon, rootView)

        bindRating(addon, rootView)
    }

    private fun bindRating(addon: Addon, rootView: View) {
        addon.rating?.let {
            val ratingView = rootView.findViewById<RatingBar>(R.id.rating_view)
            val userCountView = rootView.findViewById<TextView>(R.id.users_count)

            val ratingContentDescription =
                getString(R.string.mozac_feature_addons_rating_content_description_2)
            ratingView.contentDescription = String.format(ratingContentDescription, it.average)
            ratingView.rating = it.average

            userCountView.text = getFormattedAmount(it.reviews)
        }
    }

    private fun bindWebsite(addon: Addon, rootView: View) {
        rootView.findViewById<View>(R.id.home_page_text).setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(addon.homepageUrl))
            startActivity(intent)
        }
    }

    private fun bindLastUpdated(addon: Addon, rootView: View) {
        val lastUpdatedView = rootView.findViewById<TextView>(R.id.last_updated_text)
        lastUpdatedView.text = formatDate(addon.updatedAt)
    }

    private fun bindVersion(addon: Addon, rootView: View) {
        val versionView = rootView.findViewById<TextView>(R.id.version_text)
        versionView.text = addon.version
    }

    private fun bindAuthor(addon: Addon, rootView: View) {
        val authorsView = rootView.findViewById<TextView>(R.id.author_text)

        authorsView.text = addon.author?.name.orEmpty()
    }

    private fun bindDetails(addon: Addon, rootView: View) {
        val detailsView = rootView.findViewById<TextView>(R.id.details)
        val detailsText = addon.translateDescription(requireContext())

        val parsedText = detailsText.replace("\n", "<br/>")
        val text = HtmlCompat.fromHtml(parsedText, HtmlCompat.FROM_HTML_MODE_COMPACT)

        detailsView.text = text
        detailsView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun formatDate(text: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return DateFormat.getDateInstance().format(formatter.parse(text)!!)
    }
}