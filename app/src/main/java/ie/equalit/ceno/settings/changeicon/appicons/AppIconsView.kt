package ie.equalit.ceno.settings.changeicon.appicons

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings

class AppIconsView(
    val containerView: View,
    internal val interactor: AppIconsInteractor
) {
    val view: RecyclerView = containerView as RecyclerView

    private val appIconAdapter = AppIconsAdapter(
        interactor,
    )

    init {
        view.apply {
            adapter = appIconAdapter
            layoutManager = object : GridLayoutManager(containerView.context, 4) {
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                }
            }
            this.addItemDecoration(
                ItemOffsetDecoration(context, R.dimen.changeAppIconListPadding)
            )
        }
    }

    fun update(context: Context) {
        val selectedIconName = Settings.appIcon(context)?.componentName
        appIconAdapter.notifyChanges(selectedIconName)
    }
}