package com.acesso60.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.theme.Caution
import com.acesso60.ui.theme.Success

/** Linha "Rótulo: valor" com selo. O valor vem em texto, então a cor nunca é a única pista. */
@Composable
fun StatusRow(label: String, value: String, ok: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusBadge(
            kind = if (ok) BadgeKind.SUCCESS else BadgeKind.WARNING,
            color = if (ok) Success else Caution,
            diameter = 36.dp,
        )
        Text(
            text = stringResource(R.string.ui_status_format, label, value),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
