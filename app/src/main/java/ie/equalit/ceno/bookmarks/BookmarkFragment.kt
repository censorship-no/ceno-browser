package ie.equalit.ceno.bookmarks

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.components.StoreProvider
import ie.equalit.ceno.databinding.FragmentBookmarkBinding
import ie.equalit.ceno.databinding.FragmentStandbyBinding
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.requireComponents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.kotlin.toShortUrl
import androidx.core.graphics.drawable.toDrawable

/**
 * A simple [Fragment] subclass.
 * Use the [BookmarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookmarkFragment : Fragment(), MenuProvider {

    private lateinit var bookmarkStore: BookmarkFragmentStore

    private var _binding : FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    private var _bookmarkInteractor: BookmarkFragmentInteractor? = null
    private val bookmarkInteractor: BookmarkFragmentInteractor
        get() = _bookmarkInteractor!!

    private var mode: BookmarkFragmentState.Mode = BookmarkFragmentState.Mode.Normal()
    private var tree: BookmarkNode? = null

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    private var pendingBookmarksToDelete: MutableSet<BookmarkNode> = mutableSetOf()

    private lateinit var bookmarkAdapter:BookmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bookmarkStore = StoreProvider.get(this) {
            BookmarkFragmentStore(BookmarkFragmentState(null))
        }
        // Inflate the layout for this fragment
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        _bookmarkInteractor = BookmarkFragmentInteractor(
            bookmarksController = DefaultBookmarkController(
                activity = requireActivity() as BrowserActivity,
                navController = findNavController(),
                clipboardManager = requireContext().getSystemService(),
                scope = viewLifecycleOwner.lifecycleScope,
                store = bookmarkStore,
                sharedViewModel = sharedViewModel,
                tabsUseCases = activity?.components?.useCases?.tabsUseCases,
                loadBookmarkNode = ::loadBookmarkNode,
//                showSnackbar = ::showSnackBarWithText,
                deleteBookmarkNodes = ::deleteMulti,
                deleteBookmarkFolder = ::showRemoveFolderDialog,
//                showTabTray = ::showTabTray,
            ),
        )

        bookmarkAdapter = BookmarkAdapter(binding.bookmarksEmptyView, bookmarkInteractor)
        binding.bookmarkList.apply {
            adapter = bookmarkAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        consumeFrom(bookmarkStore) {
            update(it)
        }
    }

    fun update(state: BookmarkFragmentState) {
        tree = state.tree
        if (state.mode != mode) {
            mode = state.mode
            if (mode is BookmarkFragmentState.Mode.Normal || mode is BookmarkFragmentState.Mode.Selecting) {
//                interactor.onSelectionModeSwitch(mode)
            }
        }

        bookmarkAdapter.updateData(state.tree, mode)

//        binding.bookmarksProgressBar.isVisible = state.isLoading
    }

    override fun onResume() {
        super.onResume()

//        (activity as NavHostActivity).getSupportActionBarAndInflateIfNecessary().show()

        // Reload bookmarks when returning to this fragment in case they have been edited
        val args by navArgs<BookmarkFragmentArgs>()
        val currentGuid = bookmarkStore.state.tree?.guid
            ?: args.currentRoot.ifEmpty {
                BookmarkRoot.Mobile.id
            }
        loadInitialBookmarkFolder(currentGuid)
        getActionBar().apply {
            show()
            title = getString(R.string.library_bookmarks)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    private fun loadInitialBookmarkFolder(currentGuid: String) {
        viewLifecycleOwner.lifecycleScope.launch(Main) {
            val currentRoot = loadBookmarkNode(currentGuid)

            if (isActive && currentRoot != null) {
                bookmarkInteractor.onBookmarksChanged(currentRoot)
            }
        }
    }

    private suspend fun loadBookmarkNode(guid: String, recursive: Boolean = false): BookmarkNode? = withContext(
        IO
    ) {
        // Only runs if the fragment is attached same as [runIfFragmentIsAttached]
        context?.let {
            requireComponents.core.bookmarksStorage
                .getTree(guid, recursive)
//                ?.let { desktopFolders.withOptionalDesktopFolders(it) }
        }
    }

    private fun deleteMulti(
        selected: Set<BookmarkNode>,
        eventType: BookmarkRemoveType = BookmarkRemoveType.MULTIPLE,
    ) {
        selected.iterator().forEach {
            if (it.type == BookmarkNodeType.FOLDER) {
                showRemoveFolderDialog(selected)
                return
            }
        }
        updatePendingBookmarksToDelete(selected)

        val message = when (eventType) {
            BookmarkRemoveType.MULTIPLE -> {
//                getRemoveBookmarksSnackBarMessage(selected, containsFolders = false)
                Log.d("BOOKMARK", "Delete Multiple")
            }
            BookmarkRemoveType.FOLDER,
            BookmarkRemoveType.SINGLE,
                -> {
//                val bookmarkNode = selected.first()
//                getString(
//                    R.string.bookmark_deletion_snackbar_message,
//                    bookmarkNode.url?.toShortUrl(requireContext().components.publicSuffixList)
//                        ?: bookmarkNode.title,
//                )
                Log.d("BOOKMARK", "Delete Multiple")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch(Main) {
            getDeleteOperation(eventType).invoke(requireContext())
        }
    }

    private fun showRemoveFolderDialog(selected: Set<BookmarkNode>) {

    }

    private fun updatePendingBookmarksToDelete(selected: Set<BookmarkNode>) {
        pendingBookmarksToDelete.addAll(selected)
        val selectedFolder = sharedViewModel.selectedFolder ?: return
        val bookmarkTree = selectedFolder - pendingBookmarksToDelete
        bookmarkInteractor.onBookmarksChanged(bookmarkTree)
    }

    private fun getDeleteOperation(event: BookmarkRemoveType): (suspend (context: Context) -> Unit) {
        return { context ->
            CoroutineScope(IO).launch {
                pendingBookmarksToDelete.map {
                    async { requireComponents.core.bookmarksStorage.deleteNode(it.guid) }
                }.awaitAll()
            }
            refreshBookmarks()
        }
    }

    private suspend fun refreshBookmarks() {
        // The bookmark tree in our 'state' can be null - meaning, no bookmark tree has been selected.
        // If that's the case, we don't know what node to refresh, and so we bail out.
        // See https://github.com/mozilla-mobile/fenix/issues/4671
        val currentGuid = bookmarkStore.state.tree?.guid ?: return
        loadBookmarkNode(currentGuid)
            ?.let { node ->
                val rootNode = node - pendingBookmarksToDelete
                bookmarkInteractor.onBookmarksChanged(rootNode)
            }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        when (val mode = bookmarkStore.state.mode) {
            is BookmarkFragmentState.Mode.Normal -> {
                if (mode.showMenu) {
                    inflater.inflate(R.menu.bookmarks_menu, menu)
                }
            }
//            is BookmarkFragmentState.Mode.Selecting -> {
//                if (mode.selectedItems.any { it.type != BookmarkNodeType.ITEM }) {
//                    inflater.inflate(R.menu.bookmarks_select_multi_not_item, menu)
//                } else {
//                    inflater.inflate(R.menu.bookmarks_select_multi, menu)
//
//                    menu.findItem(R.id.delete_bookmarks_multi_select).title =
//                        SpannableString(getString(R.string.bookmark_menu_delete_button)).apply {
//                            setTextColor(requireContext(), R.attr.textCritical)
//                        }
//                }
//            }
            else -> {
                // no-op
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId) {
            R.id.add_bookmark_folder -> {
                //navigate to add folder fragment
                findNavController().navigate(R.id.action_bookmarkFragment_to_bookmarkAddFolderFragment)
                true
            }
            else -> false
        }
    }

    companion object {
        const val BOOKMARK_GUID = "guidToEdit"
    }
}