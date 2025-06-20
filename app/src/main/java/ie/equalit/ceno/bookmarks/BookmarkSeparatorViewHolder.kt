package ie.equalit.ceno.bookmarks

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R

class BookmarkSeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        @SuppressLint("NonConstantResourceId")
        const val LAYOUT_ID = R.layout.bookmark_separator
    }
}
