/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.topsites

import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.TopSiteItemBinding
import ie.equalit.ceno.ext.ceno.bitmapForUrl
import ie.equalit.ceno.ext.ceno.loadIntoView
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import ie.equalit.ceno.utils.view.CenoViewHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.top.sites.TopSite

class TopSiteItemViewHolder(
    view: View,
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: TopSiteInteractor
) : CenoViewHolder(view) {
    private lateinit var topSite: TopSite
    private val binding = TopSiteItemBinding.bind(view)

    init {
        binding.topSiteItem.setOnLongClickListener {
            interactor.onTopSiteMenuOpened()

            val topSiteMenu = TopSiteItemMenu(
                context = view.context,
                topSite = topSite
            ) { item ->
                when (item) {
                    is TopSiteItemMenu.Item.OpenInPrivateTab -> interactor.onOpenInPrivateTabClicked(
                        topSite
                    )

                    is TopSiteItemMenu.Item.RenameTopSite -> interactor.onRenameTopSiteClicked(
                        topSite
                    )

                    is TopSiteItemMenu.Item.RemoveTopSite -> interactor.onRemoveTopSiteClicked(
                        topSite
                    )

                    is TopSiteItemMenu.Item.Settings -> interactor.onSettingsClicked()
                    is TopSiteItemMenu.Item.SponsorPrivacy -> interactor.onSponsorPrivacyClicked()
                }
            }
            topSiteMenu.menuBuilder.build(view.context).show(anchor = it)
            true
        }
    }

    fun bind(topSite: TopSite, position: Int) {
        binding.topSiteItem.setOnClickListener {
            interactor.onSelectTopSite(topSite, position)
        }

        binding.topSiteTitle.text = topSite.title

        if (topSite is TopSite.Pinned || topSite is TopSite.Default) {
            val pinIndicator = getDrawable(itemView.context, R.drawable.ic_new_pin)
            binding.topSiteTitle.setCompoundDrawablesWithIntrinsicBounds(
                pinIndicator,
                null,
                null,
                null
            )
        } else {
            binding.topSiteTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

        if (topSite is TopSite.Provided) {
            binding.topSiteSubtitle.isVisible = true

            viewLifecycleOwner.lifecycleScope.launch(IO) {
                itemView.context.components.core.client.bitmapForUrl(topSite.imageUrl)
                    ?.let { bitmap ->
                        withContext(Main) {
                            binding.faviconImage.setImageBitmap(bitmap)
                        }
                    }
            }
        } else {
            /* CENO: Load built-in icons for suggested sites */
            val resources = itemView.context.resources
            when (topSite.url) {
                /* Try to match with one of the hard-coded suggested site first */
                resources.getString(R.string.suggestedsites_ceno_en_url),
                resources.getString(R.string.suggestedsites_ceno_es_url),
                resources.getString(R.string.suggestedsites_ceno_fa_url),
                resources.getString(R.string.suggestedsites_ceno_my_url),
                resources.getString(R.string.suggestedsites_ceno_ru_url),
                resources.getString(R.string.suggestedsites_ceno_uk_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_cenomanual
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_wikipedia_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_wikipedia
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_apnews_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_apnews
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_reuters_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_reuters
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_elpais_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_elpais
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_infobae_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_infobae
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_paskooceh_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_paskoocheh
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_factnameh_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_factnameh
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_courier_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_courrierinternational
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_lapresse_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_lapresse
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_mynow_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_myanmarnow
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_justicemy_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_justiceformyanmar
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_meduza_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_meduza
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_moscowtimes_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_moscowtimes
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_truestory_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_truestory
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_pravda_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_pravda
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_hromadske_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_hromadske
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_ltn_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_ltn
                        )
                    )
                }

                resources.getString(R.string.suggestedsites_twreporter_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.suggestedsites_twreporter
                        )
                    )
                }
                /* else fallback to matching url with favicon (useful for white-label suggested sites) */
                resources.getString(R.string.default_top_site_1_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.default_top_site_1_favicon
                        )
                    )
                }

                resources.getString(R.string.default_top_site_2_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.default_top_site_2_favicon
                        )
                    )
                }

                resources.getString(R.string.default_top_site_3_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.default_top_site_3_favicon
                        )
                    )
                }

                resources.getString(R.string.default_top_site_4_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.default_top_site_4_favicon
                        )
                    )
                }

                else -> {
                    itemView.context.components.core.icons.loadIntoView(
                        binding.faviconImage,
                        topSite.url
                    )
                }
            }
        }

        this.topSite = topSite
    }

    private fun onTouchEvent(
        v: View,
        event: MotionEvent,
        menu: PopupWindow
    ): Boolean {
        if (event.action == MotionEvent.ACTION_CANCEL) {
            menu.dismiss()
        }
        return v.onTouchEvent(event)
    }
}
