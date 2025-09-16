package com.yapp.ui.component.button

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OrbitButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = false,
    useFillMaxWidth: Boolean = true,
    debounceTime: Long = 500L,
    height: Dp = 54.dp,
    containerColor: Color = OrbitTheme.colors.main,
    contentColor: Color = OrbitTheme.colors.gray_900,
    pressedContainerColor: Color = OrbitTheme.colors.main.copy(alpha = 0.8f),
    pressedContentColor: Color = OrbitTheme.colors.gray_600,
    disabledContainerColor: Color = OrbitTheme.colors.gray_700,
    disabledContentColor: Color = OrbitTheme.colors.gray_600,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val coroutineScope = rememberCoroutineScope()
    var isClickable by remember { mutableStateOf(true) }

    val padding by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 100),
        label = "PaddingAnimation",
    )

    fun handleClick() {
        if (isClickable) {
            isClickable = false
            onClick()
            coroutineScope.launch {
                delay(debounceTime)
                isClickable = true
            }
        }
    }

    Button(
        onClick = ::handleClick,
        enabled = enabled && isClickable,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) pressedContainerColor else containerColor,
            contentColor = if (isPressed) pressedContentColor else contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .then(
                if (useFillMaxWidth) Modifier.fillMaxWidth() else Modifier,
            )
            .padding(padding)
            .height(height - padding * 2),
    ) {
        Text(
            text = label,
            style = OrbitTheme.typography.body1SemiBold,
        )
    }
}

@Composable
@Preview
fun OrbitButtonPreview() {
    OrbitTheme {
        OrbitButton(
            label = "label",
            modifier = Modifier,
            onClick = {},
            enabled = true,
        )
    }
}
