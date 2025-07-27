package com.yapp.home.alarm.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.radiobutton.OrbitRadioButton

@Composable
internal fun SelectorItems(
    items: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onItemSelected: (Int) -> Unit,
) {
    Box {
        Column {
            Spacer(modifier = Modifier.height(7.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 6.dp)
                    .background(
                        if (enabled) {
                            OrbitTheme.colors.gray_600
                        } else {
                            OrbitTheme.colors.gray_700
                        },
                    ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            items.forEachIndexed { index, item ->
                Column(horizontalAlignment = getAlignment(index, items.size)) {
                    OrbitRadioButton(
                        selected = index == selectedIndex,
                        enabled = enabled,
                        onClick = { if (enabled) onItemSelected(index) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item,
                        style = OrbitTheme.typography.body1Medium,
                        color = OrbitTheme.colors.gray_50,
                    )
                }
            }
        }
    }
}

private fun getAlignment(index: Int, size: Int): Alignment.Horizontal =
    when (index) {
        0 -> Alignment.Start
        size - 1 -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
