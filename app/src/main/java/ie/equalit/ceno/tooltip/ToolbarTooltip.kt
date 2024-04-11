package ie.equalit.ceno.tooltip

import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal

class ToolbarTooltip(
    var fragment: Fragment,
    var target: Int,
    listener: ( prompt:MaterialTapTargetPrompt, state:Int) -> Unit
) {
    var rectanglePromptFocal = RectanglePromptFocal()
        .setCornerRadius(25f, 25f)
    val tooltip : MaterialTapTargetPrompt.Builder

    init {
        tooltip = MaterialTapTargetPrompt.Builder(fragment)
            .setTarget(target)
            .setPrimaryText("Let's get started!")
            .setSecondaryText("Type in a website address to start browsing.")
            .setPromptFocal(rectanglePromptFocal)
            .setBackgroundColour(getColor(fragment.requireContext(), R.color.tooltip_prompt_color))
            .setFocalColour(getColor(fragment.requireContext(), R.color.tooltip_focal_color))
            .setPromptBackground(DimmedPromptBackground())
            .setPromptStateChangeListener(listener)
    }

}