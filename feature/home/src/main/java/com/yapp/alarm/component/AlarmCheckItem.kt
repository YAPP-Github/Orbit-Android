package com.yapp.alarm.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme

@Composable
internal fun AlarmCheckItem(
    label: String,
    isPressed: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(2.dp)
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = core.designsystem.R.drawable.ic_check),
            contentDescription = "Check",
            tint = if (isPressed) OrbitTheme.colors.main else OrbitTheme.colors.gray_400,
        )
        Text(
            text = label,
            style = OrbitTheme.typography.label1Medium,
            color = if (isPressed) OrbitTheme.colors.main else OrbitTheme.colors.gray_400,
            textAlign = TextAlign.Center,
        )
    }
}
