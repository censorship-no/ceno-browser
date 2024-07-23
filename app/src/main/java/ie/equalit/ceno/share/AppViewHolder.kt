package ie.equalit.ceno.share

import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.AppShareListItemBinding

class AppViewHolder(
    itemView: View,
    @get:VisibleForTesting val interactor: ShareToAppsInteractor,
) : RecyclerView.ViewHolder(itemView) {

    private var application: AppShareOption? = null

    init {
        itemView.setOnClickListener {
            application?.let { app ->
                interactor.onShareToApp(app)
            }
        }
    }

    fun bind(item: AppShareOption) {
        application = item
        val binding = AppShareListItemBinding.bind(itemView)
        binding.appName.text = item.name
        binding.appIcon.setImageDrawable(item.icon)
    }

    companion object {
        val LAYOUT_ID = R.layout.app_share_list_item
    }
}
