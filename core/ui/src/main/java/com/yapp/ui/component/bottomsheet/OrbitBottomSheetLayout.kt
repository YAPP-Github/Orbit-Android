package com.yapp.ui.component.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbitBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetState: OrbitBottomSheetState,
    shape: Shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    containerColor: Color = OrbitTheme.colors.gray_800,
    strokeColor: Color = OrbitTheme.colors.gray_700,
    strokeThickness: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    ModalBottomSheetLayout(
        modifier = modifier.navigationBarsPadding(),
        sheetState = sheetState.state,
        sheetShape = shape,
        sheetBackgroundColor = containerColor,
        sheetContent = {
            Box {
                sheetState.content?.invoke(this)
                BottomSheetTopRoundedStroke(
                    strokeColor = strokeColor,
                    strokeThickness = strokeThickness,
                )
            }
        },
    ) {
        content()
    }
}

@Composable
fun BottomSheetTopRoundedStroke(
    modifier: Modifier = Modifier,
    strokeColor: Color,
    strokeThickness: Dp = 1.dp,
    radius: Dp = 30.dp,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(radius + strokeThickness), // Stroke 고려
    ) {
        val width = size.width
        val height = size.height
        val radiusPx = radius.toPx()
        val fadeWidth = radiusPx // 양 끝에서 선이 얇아지는 범위

        val path = Path().apply {
            moveTo(0f, height) // 왼쪽 끝
            arcTo(
                rect = Rect(0f, 0f, radiusPx * 2, radiusPx * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            lineTo(width - radiusPx, 0f)
            arcTo(
                rect = Rect(width - radiusPx * 2, 0f, width, radiusPx * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }

        val pathMeasure = PathMeasure().apply { setPath(path, false) }
        val totalLength = pathMeasure.length
        val segmentCount = 100

        for (i in 0 until segmentCount) {
            val start = i * (totalLength / segmentCount)
            val end = (i + 1) * (totalLength / segmentCount)

            val segmentPath = Path()
            if (pathMeasure.getSegment(start, end, segmentPath, true)) {
                val minThickness = 0.dp.toPx()
                val maxThickness = strokeThickness.toPx()

                val thickness = when {
                    start < fadeWidth -> minThickness + (maxThickness - minThickness) * (start / fadeWidth)
                    start > totalLength - fadeWidth -> minThickness + (maxThickness - minThickness) * ((totalLength - start) / fadeWidth)
                    else -> maxThickness
                }

                drawPath(
                    path = segmentPath,
                    color = strokeColor,
                    style = Stroke(width = thickness),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun OrbitBottomSheetPreview() {
    val sheetState = rememberOrbitBottomSheetState()
    val scope = rememberCoroutineScope()

    OrbitTheme {
        OrbitBottomSheetLayout(
            sheetState = sheetState,
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = OrbitTheme.colors.white),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.show {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(500.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("This is a bottom sheet content")
                                    }
                                }
                            }
                        },
                    ) {
                        Text("Toggle Bottom Sheet")
                    }
                }
            },
        )
    }
}
