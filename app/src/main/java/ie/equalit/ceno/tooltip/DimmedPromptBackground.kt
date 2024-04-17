package ie.equalit.ceno.tooltip

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground


class DimmedPromptBackground (
    var dimBounds:RectF = RectF(),
    var dimPaint: Paint = Paint(Color.GRAY)
): CirclePromptBackground() {
    override fun prepare(
        options: PromptOptions<out PromptOptions<*>>,
        clipToBounds: Boolean,
        clipBounds: Rect
    ) {
        super.prepare(options, clipToBounds, clipBounds)
        var metrics = Resources.getSystem().displayMetrics
        // Set the bounds to display as dimmed to the screen bounds
        dimBounds[0f, 0f, metrics.widthPixels.toFloat()] = metrics.heightPixels.toFloat()
    }

    override fun update(
        options: PromptOptions<out PromptOptions<*>>,
        revealModifier: Float,
        alphaModifier: Float
    ) {
        super.update(options, revealModifier, alphaModifier)
        // Allow for the dimmed background to fade in and out
        this.dimPaint.setAlpha(((170 * alphaModifier).toInt()));
    }

    override fun draw(canvas: Canvas) {
        // Draw the dimmed background
        canvas.drawRect(this.dimBounds, this.dimPaint)
        // Draw the background
        super.draw(canvas)
    }
}