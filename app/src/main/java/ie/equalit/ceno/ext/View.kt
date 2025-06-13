package ie.equalit.ceno.ext

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP
import mozilla.components.support.ktx.android.util.dpToPx

fun View.increaseTapArea(@Dimension(unit = DP) extraDps: Int) {
    val dips = extraDps.dpToPx(resources.displayMetrics)
    val parent = this.parent as View
    parent.post {
        val touchRect = Rect()
        getHitRect(touchRect)
        touchRect.inset(-dips, -dips)
        parent.touchDelegate = TouchDelegate(touchRect, this)
    }
}