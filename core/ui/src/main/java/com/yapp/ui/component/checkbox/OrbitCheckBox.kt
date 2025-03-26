package com.yapp.ui.component.checkbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme

@Composable
fun OrbitCheckBox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
) {
    val backgroundColor = if (checked) {
        OrbitTheme.colors.main
    } else {
        OrbitTheme.colors.gray_600
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp),
            )
            .clip(RoundedCornerShape(4.dp))
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(core.designsystem.R.drawable.ic_check),
            contentDescription = "IC_CHECK",
            tint = OrbitTheme.colors.gray_700,
        )
    }
}

@Preview
@Composable
fun OrbitCheckBoxPreview() {
    OrbitTheme {
        var isChecked by remember { mutableStateOf(false) }

        OrbitCheckBox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = !isChecked
            },
        )
    }
}
