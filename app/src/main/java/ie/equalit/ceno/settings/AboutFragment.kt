/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.settings

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM
import androidx.fragment.app.Fragment
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import mozilla.components.Build
import org.mozilla.geckoview.BuildConfig.MOZ_APP_BUILDID
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VERSION


class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName = requireContext().resources.getString(R.string.app_name)
        (activity as AppCompatActivity).title = getString(R.string.preferences_about_page)

        val aboutText = try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val geckoVersion =
                PackageInfoCompat.getLongVersionCode(packageInfo).toString() + " GV: " +
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
        val content = HtmlCompat.fromHtml(
            resources.getString(R.string.about_content, appName),
            FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM,
        )

        val aboutView = view.findViewById<TextView>(R.id.about_content)
        aboutView.text = content

        val websiteButton = view.findViewById<TextView>(R.id.button1)
        setLinkTextView(websiteButton, resources.getString(R.string.website_button_text))
        websiteButton.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.website_button_link))
        )

        val sourceCodeButton = view.findViewById<TextView>(R.id.button2)
        setLinkTextView(sourceCodeButton, resources.getString(R.string.source_code_button_text))
        sourceCodeButton.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.source_code_button_link))
        )

        val donateButton = view.findViewById<TextView>(R.id.button3)
        setLinkTextView(donateButton, resources.getString(R.string.donate_button_text))
        donateButton.setOnClickListener(
            getOnClickListenerForLink(resources.getString(R.string.donate_button_link))
        )

        val versionInfoView = view.findViewById<TextView>(R.id.version_info)
        versionInfoView.text = versionInfo

        versionInfoView.setOnTouchListener { v, _ ->
            val clipBoard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipBoard.setPrimaryClip(ClipData.newPlainText(versionInfo, versionInfo))

            Toast.makeText(requireContext(), getString(R.string.toast_copied), Toast.LENGTH_SHORT)
                .show()

            v.performClick()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setLinkTextView(textView: TextView, text: String) {
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
            BackgroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.fx_mobile_text_color_secondary
                )
            ), 0, notClickedString.length,
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

    private fun getOnClickListenerForLink(url: String): View.OnClickListener {
        return View.OnClickListener {
            val browserActivity = activity as BrowserActivity
            browserActivity.openToBrowser(url, newTab = true)
            /*
            val entry: BackStackEntry =
                browserActivity.supportFragmentManager.getBackStackEntryAt(0)
            browserActivity.supportFragmentManager.popBackStack(
                entry.id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            browserActivity.supportFragmentManager.executePendingTransactions()
             */
        }
    }
}
