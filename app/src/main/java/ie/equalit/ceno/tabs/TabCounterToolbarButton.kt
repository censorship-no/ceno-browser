package ie.equalit.ceno.tabs

import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.ui.theme.ThemeManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.ui.tabcounter.TabCounter
import mozilla.components.ui.tabcounter.TabCounterMenu
import java.lang.ref.WeakReference

class TabCounterToolbarButton(
    private val store: BrowserStore,
    private val lifecycleOwner: LifecycleOwner,
    private val showTabs: () -> Unit,
    private val menu: TabCounterMenu? = null,
    private val browsingModeManager: BrowsingModeManager,
    private val countBasedOnSelectedTabType: Boolean,
    private val themeManager: ThemeManager
) : Toolbar.Action {

    private var reference = WeakReference<TabCounter>(null)

    private lateinit var tabCounter: TabCounter

    override fun bind(view: View) = Unit

    override fun createView(parent: ViewGroup): View {
        store.flowScoped(lifecycleOwner) { flow ->
            flow.map { state -> getTabCount(state) }
                .distinctUntilChanged()
                .collect { tabs ->
                    updateCount(tabs)
                }
        }


        tabCounter = TabCounter(themeManager.getContext()).apply {
            reference = WeakReference(this)
            setOnClickListener {
                showTabs.invoke()
            }

            menu?.let { menu ->
                setOnLongClickListener {
                    menu.menuController.show(anchor = it)
                    true
                }
            }

            addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        setCount(getTabCount(store.state))
                    }

                    override fun onViewDetachedFromWindow(v: View) { /* no-op */
                    }
                },
            )
            contentDescription =
                parent.context.getString(R.string.mozac_feature_tabs_toolbar_tabs_button)
        }

        // Set selectableItemBackgroundBorderless
        tabCounter.setBackgroundResource(
            parent.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
            ),
        )
        tabCounter.id = R.id.tab_counter_box

        return tabCounter
    }

    private fun getTabCount(state: BrowserState): Int {
        return if (countBasedOnSelectedTabType) {
            state.getNormalOrPrivateTabs(private = browsingModeManager.mode.isPersonal).size
        } else {
            state.tabs.size
        }
    }


    /**
     * Update the tab counter button on the toolbar.
     *
     * @property count the updated tab count
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateCount(count: Int) {
        reference.get()?.setCountWithAnimation(count)
    }

    fun updateColor() {
        tabCounter.invalidate()
    }
}