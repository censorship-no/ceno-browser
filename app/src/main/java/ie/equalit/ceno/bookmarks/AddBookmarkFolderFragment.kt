package ie.equalit.ceno.bookmarks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
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
import ie.equalit.ceno.databinding.FragmentEditBookmarkBinding
import ie.equalit.ceno.ext.requireComponents
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.support.ktx.android.view.hideKeyboard

class AddBookmarkFolderFragment : Fragment(R.layout.fragment_edit_bookmark), MenuProvider {
    private var _binding: FragmentEditBookmarkBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    /**
     * Hides fields for bookmark items present in the shared layout file.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentEditBookmarkBinding.bind(view)

        binding.bookmarkUrlLabel.visibility = GONE
        binding.bookmarkUrlEdit.visibility = GONE
        binding.inputLayoutBookmarkUrl.visibility = GONE
//        binding.bookmarkNameEdit.showKeyboard()

        viewLifecycleOwner.lifecycleScope.launch(Main) {
            val context = requireContext()
            sharedViewModel.selectedFolder = withContext(IO) {
                sharedViewModel.selectedFolder
                    ?: requireComponents.core.bookmarksStorage.getBookmark(BookmarkRoot.Mobile.id)
            }

            binding.bookmarkParentFolderSelector.text =
                friendlyRootTitle(context, sharedViewModel.selectedFolder!!)
            binding.bookmarkParentFolderSelector.setOnClickListener {
                findNavController().navigate(R.id.action_bookmarkAddFolderFragment_to_bookmarkSelectFolderFragment,
                    bundleOf("allowCreatingNewFolder" to true))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getActionBar().apply {
            show()
            title = getString(R.string.bookmark_add_folder_fragment_label)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.bookmarks_add_folder_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.confirm_add_folder_button -> {
                if (binding.bookmarkNameEdit.text.isNullOrBlank()) {
//                    binding.bookmarkNameEdit.error =
//                        getString(R.string.bookmark_empty_title_error)
                    return true
                }
                this.view?.hideKeyboard()
                viewLifecycleOwner.lifecycleScope.launch(IO) {
                    val newGuid = requireComponents.core.bookmarksStorage.addFolder(
                        sharedViewModel.selectedFolder!!.guid,
                        binding.bookmarkNameEdit.text.toString(),
                        null,
                    )
                    sharedViewModel.selectedFolder =
                        requireComponents.core.bookmarksStorage.getTree(newGuid)
                    withContext(Main) {
                        findNavController().popBackStack()
                    }
                }
                true
            }
            // other options are not handled by this menu provider
            else -> false
        }
    }
}