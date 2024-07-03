package uk.co.samuelwall.materialtaptargetprompt.extras

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.TextPaint

class PromptTextWithBtn(
    val buttonText: String,
    val buttonColor: Int
):PromptText() {

    lateinit var btnTextLayout : Layout
    var textSeparation : Float = 0.0f
    lateinit var btnRect: RectF
    var btnBounds = RectF()

    var mPaintButtonText : TextPaint = TextPaint()

    val buttonPaddingHorizontal = 25f
    val buttonPaddingVertical = 15f

    override fun prepare(
        options: PromptOptions<out PromptOptions<*>>,
        clipToBounds: Boolean,
        clipBounds: Rect
    ) {
        super.prepare(options, clipToBounds, clipBounds)
        mPaintButtonText.setColor(Color.BLACK)
        mPaintButtonText.setTextSize(options.secondaryTextSize)

        btnTextLayout = PromptUtils.createStaticTextLayout(
            buttonText,
            mPaintButtonText, mPaintButtonText.measureText(buttonText).toInt(), mPrimaryTextAlignment, 1f
        )
        textSeparation = options.textSeparation
        btnRect = RectF(0f, 0f, btnTextLayout.width.toFloat() + (buttonPaddingHorizontal * 2), btnTextLayout.height.toFloat() + (buttonPaddingVertical * 2))

        val verticalTextPositionAbove = options.promptFocal.bounds.centerY() > clipBounds.centerY()
        if (verticalTextPositionAbove) {
            mPrimaryTextTop -= (btnRect.height() + textSeparation)
            mTextBounds.top -= (btnRect.height() + textSeparation)
        } else {
            mTextBounds.bottom += (btnRect.height() + textSeparation)
        }

        btnBounds.left = mPrimaryTextLeft
        btnBounds.top = mTextBounds.top + mPrimaryTextLayout.height + textSeparation + mSecondaryTextLayout.height + textSeparation
        btnBounds.right = btnBounds.left + btnRect.width()
        btnBounds.bottom = btnBounds.top + btnRect.height()

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.translate(
            -(mPrimaryTextLeft - mPrimaryTextLeftChange)
                    + mSecondaryTextLeft - mSecondaryTextLeftChange,
            mSecondaryTextLayout.height + textSeparation
        )
        // draw a rectangle for button
        val btn_color = TextPaint()
        btn_color.setColor(buttonColor)
        canvas.drawRoundRect(btnRect, 10f, 10f, btn_color)
        canvas.translate(
            buttonPaddingHorizontal,
            buttonPaddingVertical
        )
        btnTextLayout.draw(canvas)
    }

    /*
    * Checks if the touch event is withing the button rectangle
     */
    fun isButtonPressed(x: Float, y:Float) : Boolean {
        return btnBounds.contains(x, y)
    }
}