package uk.co.samuelwall.materialtaptargetprompt.extras

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout

class PromptTextWithBtn:PromptText() {

    lateinit var btnTextLayout : Layout
    var textSeparation : Float = 0.0f
    lateinit var btnRect: RectF

    override fun prepare(
        options: PromptOptions<out PromptOptions<*>>,
        clipToBounds: Boolean,
        clipBounds: Rect
    ) {
        super.prepare(options, clipToBounds, clipBounds)
        btnTextLayout = PromptUtils.createStaticTextLayout(
            "Next",
            mPaintPrimaryText, 150, mPrimaryTextAlignment, 1f
        )
        textSeparation = options.textSeparation
        mPrimaryTextTop -= (btnTextLayout.height * 2)
        mTextBounds.top -= (btnTextLayout.height * 2)
        btnRect = RectF(0f, 0f, btnTextLayout.width.toFloat(), btnTextLayout.height.toFloat())
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.translate(
            -(mPrimaryTextLeft - mPrimaryTextLeftChange)
                    + mSecondaryTextLeft - mSecondaryTextLeftChange,
            mSecondaryTextLayout.height + textSeparation
        )
        canvas.drawRoundRect(btnRect, 10f, 10f, mPaintSecondaryText)
        btnTextLayout.draw(canvas)
    }
}