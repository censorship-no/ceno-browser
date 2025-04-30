package ie.equalit.ceno.bookmarks.edit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.equalit.ceno.R
import ie.equalit.ceno.bookmarks.BookmarksSharedViewModel
import ie.equalit.ceno.bookmarks.friendlyRootTitle
import ie.equalit.ceno.databinding.FragmentSelectBookmarkFolderBinding
import ie.equalit.ceno.ext.requireComponents
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode

class SelectBookmarkFolderFragment : Fragment(), MenuProvider {

    private var _binding: FragmentSelectBookmarkFolderBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()
    private var bookmarkNode: BookmarkNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectBookmarkFolderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onResume() {
        super.onResume()
//        showToolbar(getString(R.string.bookmark_select_folder_fragment_label))

        val args: SelectBookmarkFolderFragmentArgs by navArgs()

        viewLifecycleOwner.lifecycleScope.launch(Main) {
            bookmarkNode = withContext(IO) {
                var node = requireComponents.core.bookmarksStorage
                    .getTree(BookmarkRoot.Mobile.id, recursive = true)
                node?.copy(
                    title = friendlyRootTitle(requireContext(), node)
                )
            }
            val adapter = SelectBookmarkFolderAdapter(sharedViewModel)
            binding.recylerViewBookmarkFolders.adapter = adapter
            adapter.updateData(bookmarkNode, args.hideFolderGuid)
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        val args: SelectBookmarkFolderFragmentArgs by navArgs()
        if (!args.allowCreatingNewFolder) {
            inflater.inflate(R.menu.bookmarks_select_folder, menu)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.add_folder_button -> {
                viewLifecycleOwner.lifecycleScope.launch(Main) {
                    findNavController().navigate(R.id.action_bookmarkSelectFolderFragment_to_bookmarkAddFolderFragment)
                }
                true
            }
            // other options are not handled by this menu provider
            else -> false
        }
    }
}