/*
 * Copyright (c) 2023 DuckDuckGo, eQualitie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ie.equalit.ceno.settings.changeicon.appicons

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.changeicon.appicons.IconModifier.Companion.QUALIFIER

interface IconModifier {

    companion object {
        const val QUALIFIER = "ie.equalit.ceno"
    }

    fun changeIcon(
        previousIcon: AppIcon,
        newIcon: AppIcon,
    )

    fun isEnabled(icon: AppIcon): Boolean
}

enum class AppIcon(
    val componentName: String, // Must correspond to the <activity-alias> `android:name`s in AndroidManifest
    @DrawableRes val icon: Int = R.drawable.ic_app_icon_blue_round,
) {
    DEFAULT(
        componentName = "$QUALIFIER.Launcher",
        icon = R.drawable.ic_app_icon_blue_round,
    ),
    WHITE(
        componentName = "$QUALIFIER.LauncherWhite",
        icon = R.drawable.ic_app_icon_white_round,
    ),
    RED(
        componentName = "$QUALIFIER.LauncherRed",
        icon = R.drawable.ic_app_icon_red_round,
    ),
    CAMO_1(
        componentName = "$QUALIFIER.LauncherCamo1",
        icon = R.drawable.ic_app_icon_camo_1_round,
    ),
    CAMO_2(
        componentName = "$QUALIFIER.LauncherCamo2",
        icon = R.drawable.ic_app_icon_camo_2_round,
    ),
    CAMO_3(
        componentName = "$QUALIFIER.LauncherCamo3",
        icon = R.drawable.ic_app_icon_camo_3_round,
    ),
    CAMO_4(
        componentName = "$QUALIFIER.LauncherCamo4",
        icon = R.drawable.ic_app_icon_camo_4_round,
    ),
    CAMO_5(
        componentName = "$QUALIFIER.LauncherCamo5",
        icon = R.drawable.ic_app_icon_camo_5_round,
    ),
    ;

    companion object {
        fun from(componentName: String): AppIcon {
            return values().first { it.componentName == componentName }
        }
    }
}

class AppIconModifier(
    private val context: Context,
) : IconModifier {

    override fun changeIcon(
        previousIcon: AppIcon,
        newIcon: AppIcon,
    ) {
        disable(context, newIcon)
        enable(context, newIcon)
    }

    override fun isEnabled(icon: AppIcon): Boolean {
        return getComponentState(
            context,
            icon.componentName
        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    }

    private fun enable(
        context: Context,
        appIcon: AppIcon,
    ) {
        setComponentState(
            context,
            appIcon.componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        )
    }

    private fun disable(
        context: Context,
        appIcon: AppIcon,
    ) {
        AppIcon.values().filterNot { it.componentName == appIcon.componentName }.forEach {
            setComponentState(
                context,
                it.componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            )
        }
    }

    private fun getComponentState(context: Context, componentName: String): Int {
        return context.packageManager.getComponentEnabledSetting(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                componentName
            )
        )
    }

    private fun setComponentState(
        context: Context,
        componentName: String,
        componentState: Int,
    ) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(BuildConfig.APPLICATION_ID, componentName),
            componentState,
            PackageManager.DONT_KILL_APP,
        )
    }
}
