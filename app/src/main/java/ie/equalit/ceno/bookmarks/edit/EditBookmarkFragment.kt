package ie.equalit.ceno.bookmarks.edit

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.bookmarks.BookmarkFragment
import ie.equalit.ceno.bookmarks.BookmarksSharedViewModel
import ie.equalit.ceno.bookmarks.friendlyRootTitle
import ie.equalit.ceno.databinding.FragmentEditBookmarkBinding
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.requireComponents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.uniffi.PlacesApiException
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.ktx.android.view.hideKeyboard

class EditBookmarkFragment : Fragment(R.layout.fragment_edit_bookmark), MenuProvider {

    private var _binding: FragmentEditBookmarkBinding? = null
    private val binding get() = _binding!!

    private var bookmarkNode: BookmarkNode? = null
    private var bookmarkParent: BookmarkNode? = null
    private var initialParentGuid: String? = null
    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()
    lateinit var actionbarTitle:String

    val guidToEdit: String?
        get() = arguments?.getString(BookmarkFragment.BOOKMARK_GUID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBookmarkBinding.inflate(inflater, container, false)
        container?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ceno_standby_background))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val bookmarkNodeBeforeReload = bookmarkNode
            val bookmarksStorage = context.components.core.bookmarksStorage

            bookmarkNode = withContext(Dispatchers.IO) {
                guidToEdit?.let { bookmarksStorage.getBookmark(it) }
            }

            if (initialParentGuid == null) {
                initialParentGuid = bookmarkNode?.parentGuid
            }

            bookmarkParent = withContext(Dispatchers.IO) {
                // Use user-selected parent folder if it's set, or node's current parent otherwise.
                if (sharedViewModel.selectedFolder != null) {
                    sharedViewModel.selectedFolder
                } else {
                    bookmarkNode?.parentGuid?.let { bookmarksStorage.getBookmark(it) }
                }
            }

            when (bookmarkNode?.type) {
                BookmarkNodeType.FOLDER -> {
                    actionbarTitle= getString(R.string.edit_bookmark_folder_fragment_title)
                    binding.inputLayoutBookmarkUrl.visibility = View.GONE
                    binding.bookmarkUrlEdit.visibility = View.GONE
                    binding.bookmarkUrlLabel.visibility = View.GONE
                }
                BookmarkNodeType.ITEM -> {
                    actionbarTitle = getString(R.string.edit_bookmark_fragment_title)
                }
                else -> throw IllegalArgumentException()
            }

            val currentBookmarkNode = bookmarkNode
            if (currentBookmarkNode != null && currentBookmarkNode != bookmarkNodeBeforeReload) {
                binding.bookmarkNameEdit.setText(currentBookmarkNode.title)
                binding.bookmarkUrlEdit.setText(currentBookmarkNode.url)
            }

            bookmarkParent?.let { node ->
                binding.bookmarkParentFolderSelector.text = friendlyRootTitle(context, node)
            }
            binding.bookmarkParentFolderSelector.setOnClickListener {
                sharedViewModel.selectedFolder = null
                findNavController().navigate(
                    R.id.action_editBookmarkFragment_to_bookmarkSelectFolderFragment,
                    bundleOf(
                        "allowCreatingNewFolder" to false,
                        "hideFolderGuid" to when (bookmarkNode!!.type) {
                            BookmarkNodeType.FOLDER -> bookmarkNode!!.guid
                            else -> null
                        }
                        )
                )
            }

            binding.bookmarkNameEdit.apply {
                requestFocus()
            }
            (activity as AppCompatActivity).supportActionBar!!.apply {
                show()
                title = actionbarTitle
                setDisplayHomeAsUpEnabled(true)
                setBackgroundDrawable(
                    ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
//        if (requireContext().settings().useNewBookmarks) {
//            return
//        }
        binding.bookmarkNameEdit.hideKeyboard()
        binding.bookmarkUrlEdit.hideKeyboard()
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
//        if (requireContext().settings().useNewBookmarks) {
//            return
//        }
        inflater.inflate(R.menu.bookmarks_edit, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_bookmark_button -> {
                displayDeleteBookmarkDialog()
                true
            }
            R.id.save_bookmark_button -> {
                updateBookmarkFromTextChanges()
                true
            }

            // other options are not handled by this menu provider
            else -> false
        }
    }
    private fun displayDeleteBookmarkDialog() {
        activity?.let { activity ->
            AlertDialog.Builder(activity).apply {
                setMessage(R.string.bookmark_deletion_confirmation)
                setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ ->
                    dialog.cancel()
                }
                setPositiveButton(R.string.dialog_btn_positive_ok) { dialog: DialogInterface, _ ->
                    // Use fragment's lifecycle; the view may be gone by the time dialog is interacted with.
                    lifecycleScope.launch(IO) {
                        requireComponents.core.bookmarksStorage.deleteNode(guidToEdit!!)

                        launch(Main) {
                            findNavController().popBackStack()

                            bookmarkNode?.let { bookmark ->
                                Toast.makeText(context, "Bookmark Deleted!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    dialog.dismiss()
                }
                create()
            }.show()
        }
    }

    private fun updateBookmarkFromTextChanges() {
        val nameText = binding.bookmarkNameEdit.text.toString()
        val urlText = binding.bookmarkUrlEdit.text.toString()
        updateBookmarkNode(nameText, urlText)
    }

    private fun updateBookmarkNode(title: String?, url: String?) {
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            try {
                requireComponents.let { components ->
                    val parentGuid =
                        sharedViewModel.selectedFolder?.guid ?: bookmarkNode!!.parentGuid
                    val parentChanged = initialParentGuid != parentGuid
                    guidToEdit?.let {
                        components.core.bookmarksStorage.updateNode(
                            it,
                            BookmarkInfo(
                                parentGuid,
                                // Setting position to 'null' is treated as a 'move to the end' by the storage API.
                                if (parentChanged) null else bookmarkNode?.position,
                                title,
                                if (bookmarkNode?.type == BookmarkNodeType.ITEM) url else null,
                            ),
                        )
                    }
                }
                withContext(Main) {
                    binding.inputLayoutBookmarkUrl.error = null
                    binding.inputLayoutBookmarkUrl.errorIconDrawable = null

                    findNavController().popBackStack()
                }
            } catch (e: PlacesApiException.UrlParseFailed) {
                withContext(Main) {
                    binding.inputLayoutBookmarkUrl.error =
                        getString(R.string.bookmark_invalid_url_error)
                    binding.inputLayoutBookmarkUrl.setErrorIconDrawable(R.drawable.mozac_ic_warning_24)
                    binding.inputLayoutBookmarkUrl.setErrorIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.fx_mobile_text_color_warning,
                            ),
                        ),
                    )
                }
            }
        }
    }
}