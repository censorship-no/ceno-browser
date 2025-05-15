/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.annotation.StringRes
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.Log.Priority.WARN
import ie.equalit.ceno.BrowserApplication
import ie.equalit.ceno.Components
import ie.equalit.ceno.R

/**
 * Get the BrowserApplication object from a context.
 */
val Context.application: BrowserApplication
    get() = applicationContext as BrowserApplication

/**
 * Get the requireComponents of this application.
 */
val Context.components: Components
    get() = application.components

fun Context.getPreferenceKey(@StringRes resourceId: Int): String =
    resources.getString(resourceId)

fun Context.cenoPreferences() = components.cenoPreferences

/**
 *  Shares content via [ACTION_SEND] intent.
 *
 * @param text the data to be shared  [EXTRA_TEXT]
 * @param subject of the intent [EXTRA_TEXT]
 * @return true it is able to share false otherwise.
 */
fun Context.share(text: String, subject: String = ""): Boolean {
    return try {
        val intent = Intent(ACTION_SEND).apply {
            type = "text/plain"
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_TEXT, text)
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        val shareIntent = Intent.createChooser(intent, getString(R.string.menu_share_with)).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(shareIntent)
        true
    } catch (e: ActivityNotFoundException) {
        Log.log(WARN, message = "No activity to share to found", throwable = e, tag = "Reference-Browser")
        false
    }
}


// Custom implementation for creating different segments inside a horizontal LinearLayout
fun Context.createSegment(percentage: Float, @ColorRes background: Int): View {
    val segment = View(this)
    val layoutParams = LinearLayout.LayoutParams(
        0,
        LinearLayout.LayoutParams.MATCH_PARENT,
        percentage
    )
    segment.layoutParams = layoutParams
    segment.setBackgroundColor(ContextCompat.getColor(this, background))
    return segment
}

fun Context.isFirstInstall(): Boolean {
    try {
        val firstInstallTime: Long = packageManager
            .getPackageInfo(packageName, 0).firstInstallTime
        val lastUpdateTime: Long = packageManager
            .getPackageInfo(packageName, 0).lastUpdateTime
        return firstInstallTime == lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return true
    }
}
fun Context.isInstallFromUpdate(): Boolean {
    try {
        val firstInstallTime: Long = packageManager
            .getPackageInfo(packageName, 0).firstInstallTime
        val lastUpdateTime: Long = packageManager
            .getPackageInfo(packageName, 0).lastUpdateTime
        return firstInstallTime != lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return false
    }
}