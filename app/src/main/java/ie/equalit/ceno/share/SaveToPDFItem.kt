package ie.equalit.ceno.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ie.equalit.ceno.R

/**
 * A save to PDF item.
 *
 *  @param onClick event handler when the save to PDF item is clicked.
 */
@Composable
fun SaveToPDFItem(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))

        Icon(
            painter = painterResource(R.drawable.mozac_ic_download_24),
            contentDescription = null,
            tint = colorResource(id = R.color.fx_mobile_icon_color_primary)
        )

        Spacer(Modifier.width(32.dp))

        Text(
            color = colorResource(id = R.color.fx_mobile_text_color_primary),
            text = stringResource(R.string.share_save_to_pdf),
        )
    }
}
