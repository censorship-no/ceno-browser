/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import ie.equalit.ceno.ext.components
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.feature.autofill.ui.AbstractAutofillConfirmActivity

@RequiresApi(Build.VERSION_CODES.O)
class AutofillConfirmActivity : AbstractAutofillConfirmActivity() {
    override val configuration: AutofillConfiguration by lazy { components.autofillConfiguration }
}
