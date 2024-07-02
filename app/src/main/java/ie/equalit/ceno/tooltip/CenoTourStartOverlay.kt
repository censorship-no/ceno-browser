package ie.equalit.ceno.tooltip

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R

class CenoTourStartOverlay(
    val fragment: Fragment,
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
    }

    fun show() {
        containerView?.addView(tooltipOverlay)
    }

    fun remove() {
        containerView?.removeView(tooltipOverlay)
    }
}