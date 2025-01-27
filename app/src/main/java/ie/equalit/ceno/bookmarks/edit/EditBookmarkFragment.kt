package ie.equalit.ceno.bookmarks.edit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentEditBookmarkBinding

class EditBookmarkFragment : Fragment() {

    private var _binding: FragmentEditBookmarkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_bookmark, container, false)
    }
}