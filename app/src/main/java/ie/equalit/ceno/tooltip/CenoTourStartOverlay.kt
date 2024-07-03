package ie.equalit.ceno.tooltip

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R

class CenoTourStartOverlay(
    val fragment: Fragment,
    isPermission: Boolean,
    skipListener:  () -> Unit,
    startListener:  () -> Unit
) {

    private var tooltipOverlay: ConstraintLayout
    private var containerView: FrameLayout?
    private var btnStartTour: Button
    private var btnSkipTour: Button

    init {
        containerView = fragment.activity?.findViewById(R.id.container)
        tooltipOverlay = View.inflate(fragment.requireContext(), R.layout.tooltip_start_overlay_layout, null) as ConstraintLayout
        tooltipOverlay.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.tooltip_background))
        btnSkipTour = tooltipOverlay.findViewById(R.id.btn_skip_all_ceno_tour)
        btnStartTour = tooltipOverlay.findViewById(R.id.btn_start_ceno_tour)
        btnSkipTour.setOnClickListener {
            remove()
            skipListener.invoke()
        }
        btnStartTour.setOnClickListener {
            remove()
            startListener.invoke()
        }
        if (isPermission) {
            //change text
            val title = tooltipOverlay.findViewById<TextView>(R.id.tv_start_tooltip_title)
            val description = tooltipOverlay.findViewById<TextView>(R.id.tv_start_tooltip_description)
            title.text = fragment.getString(R.string.onboarding_permissions_title)
            description.text = fragment.getString(R.string.onboarding_battery_text_v33)
            btnStartTour.text = fragment.getString(R.string.onboarding_battery_button)
            //hide skip button
            btnSkipTour.visibility = View.GONE
        }
    }

    fun show() {
        containerView?.addView(tooltipOverlay)
    }

    fun remove() {
        containerView?.removeView(tooltipOverlay)
    }
}