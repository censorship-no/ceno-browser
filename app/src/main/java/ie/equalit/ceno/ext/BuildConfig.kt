/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ext

import ie.equalit.ceno.BuildConfig

val isCrashReportActive: Boolean
    get() = !BuildConfig.DEBUG && BuildConfig.CRASH_REPORTING_ENABLED
