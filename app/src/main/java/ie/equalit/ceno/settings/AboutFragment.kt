/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.Fragment
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentAboutBinding
import mozilla.components.Build
import org.mozilla.geckoview.BuildConfig.MOZ_APP_BUILDID
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VERSION


class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName = requireContext().resources.getString(R.string.app_name)
        (activity as AppCompatActivity).title = getString(R.string.preferences_about_page)

        val aboutText = try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val geckoVersion = PackageInfoCompat.getLongVersionCode(packageInfo).toString() + " GV: " +
                MOZ_APP_VERSION + "-" + MOZ_APP_BUILDID
            String.format(
                "%s (Build #%s)\n",
                packageInfo.versionName,
                geckoVersion,
            )
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }

        val versionInfo = String.format(
            "%s \uD83D\uDCE6: %s, %s\n\uD83D\uDEA2: %s",
            aboutText,
            Build.version,
            Build.gitHash,
            Build.applicationServicesVersion,
        )

        setLinkTextView(requireContext(), binding.btnWebsite, resources.getString(R.string.website_button_text))
        binding.btnWebsite.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.website_button_link))
        )

        setLinkTextView(requireContext(), binding.btnSourcecode, resources.getString(R.string.source_code_button_text))
        binding.btnSourcecode.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.source_code_button_link))
        )
        setLinkTextView(requireContext(), binding.btnSupport, resources.getString(R.string.support_button_text))
        binding.btnSupport.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.support_button_link))
        )

        setLinkTextView(requireContext(), binding.btnWebsiteEq, resources.getString(R.string.website_button_text))
        binding.btnWebsiteEq.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.website_eq_button_link))
        )
        setLinkTextView(requireContext(), binding.btnNews, resources.getString(R.string.eq_news_button_text))
        binding.btnNews.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.eq_news_button_link))
        )

        setLinkTextView(requireContext(), binding.btnValues, resources.getString(R.string.eq_values_button_text))
        binding.btnValues.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.eq_values_button_link))
        )

        binding.versionInfo.text = versionInfo
    }

    companion object {
        @SuppressLint("ClickableViewAccessibility")
        fun setLinkTextView (context: Context, textView : TextView, text : String) {
            val notClickedString = SpannableString(text)
            notClickedString.setSpan(
                URLSpan(""),
                0,
                notClickedString.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.setText(notClickedString, TextView.BufferType.SPANNABLE)
            val clickedString = SpannableString(notClickedString)
            clickedString.setSpan(
                BackgroundColorSpan(ContextCompat.getColor(context, R.color.fx_mobile_text_color_secondary)), 0, notClickedString.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            textView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> textView.text = clickedString
                    MotionEvent.ACTION_UP -> {
                        textView.setText(notClickedString, TextView.BufferType.SPANNABLE)
                        v.performClick()
                    }
                    MotionEvent.ACTION_CANCEL -> textView.setText(
                        notClickedString,
                        TextView.BufferType.SPANNABLE
                    )
                }
                true
            }
        }
    }

    private fun getOnClickListenerForLink (url : String) : View.OnClickListener {
        return View.OnClickListener {
            val browserActivity = activity as BrowserActivity
            browserActivity.openToBrowser(url, newTab = true)
        }
    }
}
