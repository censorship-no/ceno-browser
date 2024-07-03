package ie.equalit.ceno.tooltip

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptTextWithBtn

class CenoTooltip(
    var fragment: Fragment,
    var target: Int,
    var primaryText: String,
    var secondaryText: String,
    var promptFocal: PromptFocal,
    isAutoFinish: Boolean = false,
    stopCaptureTouchOnFocal: Boolean = false,
    listener: ( prompt:MaterialTapTargetPrompt, state:Int) -> Unit,
    var onButtonPressListener: () -> Unit
) {
    var tooltip: MaterialTapTargetPrompt?
    val tooltipBuilder : MaterialTapTargetPrompt.Builder
    val promptTextWithBtn = PromptTextWithBtn(
        fragment.getString(R.string.btn_next),
        getColor(fragment.requireContext(), R.color.ceno_blue_100)
    )
    var isButtonPressed:Boolean = false

    init {
        tooltipBuilder = MaterialTapTargetPrompt.Builder(fragment)
            .setTarget(target)
            .setPrimaryText(primaryText)
            .setSecondaryText(secondaryText)
            .setPromptFocal(promptFocal)
            .setPromptText(promptTextWithBtn)
            .setAutoDismiss(false)
            .setBackgroundColour(getColor(fragment.requireContext(), R.color.tooltip_prompt_color))
            .setFocalColour(getColor(fragment.requireContext(), R.color.tooltip_focal_color))
            .setPromptBackground(DimmedPromptBackground(promptButtonCallback = ::promptButtonCallback))
            .setPromptStateChangeListener(listener)
            .setAutoFinish(isAutoFinish)
            .setCaptureTouchEventOnFocal(stopCaptureTouchOnFocal)
        tooltip = tooltipBuilder.create()

    }

    fun promptButtonCallback(x:Float, y:Float) {
        if (promptTextWithBtn.isButtonPressed(x, y) && !isButtonPressed) {
            isButtonPressed = true
            onButtonPressListener.invoke()
            //dismiss tooltip
            dismiss()
        }

    }

    fun addExitButton(listener: View.OnClickListener) {
        val container = fragment.activity?.findViewById<FrameLayout>(R.id.container)
        val prompt = container?.findViewById<View>(R.id.material_target_prompt_view)
        container?.removeView(prompt)
        val tooltipOverlay = View.inflate(fragment.requireContext(), R.layout.tooltip_overlay_layout, null) as ConstraintLayout
        tooltipOverlay.addView(prompt, 0)
        container?.addView(tooltipOverlay)
        val btnSkip = tooltipOverlay.findViewById<Button>(R.id.btn_skip_tour)
        btnSkip.setOnClickListener(listener)
    }

    fun dismiss() {
        val container = fragment.activity?.findViewById<FrameLayout>(R.id.container)
        container?.removeView(container.findViewById(R.id.tooltip_overlay_layout))
    }

    fun hideButtons() {
        val overlay = fragment.activity?.findViewById<ConstraintLayout>(R.id.tooltip_overlay_layout)
        overlay?.visibility = View.GONE
    }
}