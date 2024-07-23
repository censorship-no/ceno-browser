package ie.equalit.ceno.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ie.equalit.ceno.databinding.FragmentShareBinding
import ie.equalit.ceno.ext.requireComponents
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.feature.share.RecentAppsStorage

class ShareFragment : BottomSheetDialogFragment() {

    private val args by navArgs<ShareFragmentArgs>()
    private val viewModel: ShareViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }
    private lateinit var shareInteractor: ShareInteractor
    private lateinit var shareCloseView: ShareCloseView
    private lateinit var shareToAppsView: ShareToAppsView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.loadDevicesAndApps(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NO_TITLE, R.style.ShareDialogStyle)
    }

    override fun onPause() {
        super.onPause()
        consumePrompt { onDismiss() }
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireComponents.useCases.sessionUseCases.exitFullscreen.invoke()
        val binding = FragmentShareBinding.inflate(
            inflater,
            container,
            false,
        )
        val shareData = args.data.toList()

        shareInteractor = ShareInteractor(
            DefaultShareController(
                context = requireContext(),
                shareSubject = args.shareSubject,
                shareData = shareData,
                navController = findNavController(),
                saveToPdfUseCase = requireComponents.useCases.sessionUseCases.saveToPdf,
                printUseCase = requireComponents.useCases.sessionUseCases.printContent,
                recentAppsStorage = RecentAppsStorage(requireContext()),
                viewLifecycleScope = viewLifecycleOwner.lifecycleScope,
            ) { result ->
                consumePrompt {
                    when (result) {
                        ShareController.Result.DISMISSED -> onDismiss()
                        ShareController.Result.SHARE_ERROR -> onFailure()
                        ShareController.Result.SUCCESS -> onSuccess()
                    }
                }
                super.dismiss()
            },
        )
        binding.shareWrapper.setOnClickListener { shareInteractor.onShareClosed() }

        if (args.showPage) {
            // Show the previous fragment underneath the share background scrim
            // by making it translucent.
            binding.closeSharingScrim.alpha = SHOW_PAGE_ALPHA
            binding.shareWrapper.setOnClickListener { shareInteractor.onShareClosed() }
        } else {
            // Otherwise, show a list of tabs to share.
            binding.closeSharingScrim.alpha = 1.0f
            shareCloseView = ShareCloseView(binding.closeSharingContent, shareInteractor)
            shareCloseView.setTabs(shareData)
        }
        shareToAppsView = ShareToAppsView(binding.appsShareLayout, shareInteractor)

        binding.savePdf.setContent {
            SaveToPDFItem {
                shareInteractor.onSaveToPDF(tabId = args.sessionId)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.appsList.observe(viewLifecycleOwner) { appsToShareTo ->
            shareToAppsView.setShareTargets(appsToShareTo)
        }
        viewModel.recentAppsList.observe(viewLifecycleOwner) { appsToShareTo ->
            shareToAppsView.setRecentShareTargets(appsToShareTo)
        }
    }

    override fun onDestroy() {
        setFragmentResult(RESULT_KEY, Bundle())
        // Clear the stored result in case there is no listener with the same key set.
        clearFragmentResult(RESULT_KEY)

        super.onDestroy()
    }

    /**
     * If [ShareFragmentArgs.sessionId] is set and the session has a pending Web Share
     * prompt request, call [consume] then clean up the prompt.
     */
    private fun consumePrompt(
        consume: PromptRequest.Share.() -> Unit,
    ) {
        val browserStore = requireComponents.core.store
        args.sessionId
            ?.let { sessionId -> browserStore.state.findTabOrCustomTab(sessionId) }
            ?.let { tab ->
                val promptRequest =
                    tab.content.promptRequests.lastOrNull { it is PromptRequest.Share }
                if (promptRequest is PromptRequest.Share) {
                    consume(promptRequest)
                    browserStore.dispatch(
                        ContentAction.ConsumePromptRequestAction(
                            tab.id,
                            promptRequest
                        )
                    )
                }
            }
    }

    companion object {
        const val SHOW_PAGE_ALPHA = 0.6f
        const val RESULT_KEY = "shareFragmentResultKey"
    }
}