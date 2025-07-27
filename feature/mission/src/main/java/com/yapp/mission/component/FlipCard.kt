package com.yapp.mission.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.yapp.mission.MissionContract

@Composable
fun FlipCard(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    val rotationZ = remember { Animatable(0f) }
    val rotationY = remember { Animatable(state.rotationY) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(state.isFlipped) {
        if (state.isFlipped) {
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )
            rotationY.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            )
        } else {
            rotationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            )
        }
    }

    LaunchedEffect(state.currentCount) {
        if (state.currentCount in 1..state.missionCount - 1) {
            rotationZ.animateTo(
                targetValue = -20f,
                animationSpec = tween(durationMillis = 66, easing = LinearEasing),
            )
            rotationZ.animateTo(
                targetValue = 20f,
                animationSpec = tween(durationMillis = 133, easing = LinearEasing),
            )
            rotationZ.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 66, easing = LinearEasing),
            )
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .graphicsLayer(
                cameraDistance = 12f * LocalDensity.current.density,
                rotationZ = rotationZ.value,
                rotationY = rotationY.value,
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (rotationY.value <= 90f) {
            Image(
                painter = painterResource(id = core.designsystem.R.drawable.ic_amulet_front),
                contentDescription = null,
                modifier = Modifier.wrapContentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = core.designsystem.R.drawable.ic_amulet_back),
                contentDescription = null,
                modifier = Modifier.wrapContentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
@Preview
fun FlipCardPreview() {
    val state = MissionContract.State()
    val rotationY by animateFloatAsState(targetValue = state.rotationY, animationSpec = tween(1000))
    val rotationZ by animateFloatAsState(targetValue = state.rotationZ, animationSpec = tween(1000))

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        FlipCard(
            state = state.copy(rotationY = rotationY, rotationZ = rotationZ),
            eventDispatcher = {},
        )
    }
}
