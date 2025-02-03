package ie.equalit.ceno.settings.dialogs

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ie.equalit.ceno.databinding.DialogWebViewPopupSheetBinding
import ie.equalit.ceno.ext.components
import mozilla.components.concept.engine.EngineSession

class WebViewPopupPanel(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val url: String,
) : BottomSheetDialog(context), EngineSession.Observer {

    private var engineSession: EngineSession? = null
    private var canGoBack = false
    private var binding: DialogWebViewPopupSheetBinding =
        DialogWebViewPopupSheetBinding.inflate(layoutInflater, null, false)

    init {
        context.components.metrics.autoTracker.measureVisit(listOf(TAG))
        initWindow()
        setContentView(binding.root)
        expand()
        updateWebView()
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

    private fun updateWebView() {
        engineSession = context.components.core.engine.createSession(true)
        engineSession!!.loadUrl(url)
        engineSession!!.register(object : EngineSession.Observer {
            override fun onNavigationStateChange(canGoBack: Boolean?, canGoForward: Boolean?) {
                super.onNavigationStateChange(canGoBack, canGoForward)
                if (canGoBack != null) {
                    this@WebViewPopupPanel.canGoBack = canGoBack
                }
            }

            override fun onProgress(progress: Int) {
                super.onProgress(progress)
                if (progress == 100) {
                    binding.progressCircle.isVisible = false
                }
            }
        })
        binding.addonSettingsEngineView.render(engineSession!!)
        binding.backButton.setOnClickListener {
            if (canGoBack)
                engineSession!!.goBack()
            else {
                this.dismiss()
            }
        }
    }
    companion object {
        private const val TAG = "LanguageChangeDialog"
    }
}
