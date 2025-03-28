package ie.equalit.ceno.addons

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import ie.equalit.ceno.ext.createSegment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BaseBrowserFragment
import ie.equalit.ceno.databinding.DialogWebExtensionPopupSheetBinding
import ie.equalit.ceno.ext.components
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.concept.engine.EngineSession
import mozilla.components.support.ktx.android.view.putCompoundDrawablesRelativeWithIntrinsicBounds
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl
import org.json.JSONObject
import java.util.Locale


class WebExtensionActionPopupPanel(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val tabUrl: String,
    private val isConnectionSecure: Boolean,
    sourceCounts: JSONObject?
) : BottomSheetDialog(context), EngineSession.Observer {

    private var binding: DialogWebExtensionPopupSheetBinding =
        DialogWebExtensionPopupSheetBinding.inflate(layoutInflater, null, false)

    init {
        initWindow()
        setContentView(binding.root)
        expand()
        updateTitle()
        updateConnectionState()
        sourceCounts?.let { c -> onCountsFetched(c) }
    }

    private fun initWindow() {
        this.window?.decorView?.let {
            it.setViewTreeLifecycleOwner(lifecycleOwner)
            it.setViewTreeSavedStateRegistryOwner(
                lifecycleOwner as SavedStateRegistryOwner,
            )
        }
    }

    private fun expand() {
        val bottomSheet =
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateTitle() {
        binding.siteTitle.text = tabUrl.tryGetHostFromUrl()
        context.components.core.icons.loadIntoView(
            binding.siteFavicon,
            IconRequest(tabUrl, isPrivate = true),
        )
    }

    private fun updateConnectionState() {
        binding.securityInfo.text = if (isConnectionSecure) {
            context.getString(R.string.secure_connection)
        } else {
            context.getString(R.string.insecure_connection)
        }

        // TODO: if more info about HTTPS is added, bring back this icon to indicate more info is available
        //val nextIcon = AppCompatResources.getDrawable(context, R.drawable.mozac_ic_arrowhead_right)

        val securityIcon = if (isConnectionSecure) {
            AppCompatResources.getDrawable(context, R.drawable.mozac_ic_lock_24)
        } else {
            AppCompatResources.getDrawable(context, R.drawable.mozac_ic_lock_slash_24)
        }

        binding.securityInfo.putCompoundDrawablesRelativeWithIntrinsicBounds(
            start = securityIcon,
            end = null,
            top = null,
            bottom = null,
        )
    }

    fun onCountsFetched(counts : JSONObject) {
        binding.progressBar.isGone = context.components.core.store.state.selectedTab?.content?.loading == false

        val distCache = if (counts.has(BaseBrowserFragment.DIST_CACHE)) counts.getString(BaseBrowserFragment.DIST_CACHE).toFloat() else 0F
        val proxy = if (counts.has(BaseBrowserFragment.PROXY)) counts.getString(BaseBrowserFragment.PROXY).toFloat() else 0F
        val injector = if (counts.has(BaseBrowserFragment.INJECTOR)) counts.getString(BaseBrowserFragment.INJECTOR).toFloat() else 0F
        val origin = if (counts.has(BaseBrowserFragment.ORIGIN)) counts.getString(BaseBrowserFragment.ORIGIN).toFloat() else 0F
        val localCache = if (counts.has(BaseBrowserFragment.LOCAL_CACHE)) counts.getString(BaseBrowserFragment.LOCAL_CACHE).toFloat() else 0F

        binding.tvViaCenoNetworkCount.text = String.format(Locale.getDefault(),"%d", proxy.plus(injector).toInt())
        binding.tvViaCenoCacheCount.text = String.format(Locale.getDefault(),"%d", distCache.toInt())
        binding.tvDirectFromWebsiteCount.text = String.format(Locale.getDefault(),"%d", origin.toInt())

        if (localCache > 0F) {
            //show local cache
            binding.tvViaLocalCacheCount.visibility = View.VISIBLE
            binding.tvViaLocalCache.visibility = View.VISIBLE
            binding.tvViaLocalCacheCount.text = String.format(Locale.getDefault(),"%d", localCache.toInt())
        } else {
            binding.tvViaLocalCacheCount.visibility = View.GONE
            binding.tvViaLocalCache.visibility = View.GONE
        }


        val sum = distCache + origin + injector + proxy
        binding.sourcesProgressBar.isVisible = sum != 0F

        binding.sourcesProgressBar.removeAllViews()

        if(origin > 0) binding.sourcesProgressBar.addView(context.createSegment(((origin / sum) * 100), R.color.ceno_sources_green))
        if((proxy + injector) > 0) binding.sourcesProgressBar.addView(context.createSegment((((proxy + injector) / sum) * 100), R.color.ceno_sources_orange))
        if(distCache > 0) binding.sourcesProgressBar.addView(context.createSegment(((distCache / sum) * 100), R.color.ceno_sources_blue))
//                if(localCache > 0) binding.sourcesProgressBar.addView(createSegment((localCache / sum) * 100, R.color.ceno_sources_yellow))
        //}
    }
}
