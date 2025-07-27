package com.yapp.home.alarm.component.bottomsheet

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.AlarmSound
import com.yapp.ui.component.OrbitBottomSheet
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.radiobutton.OrbitRadioButton
import com.yapp.ui.component.slider.OrbitSlider
import com.yapp.ui.component.switch.OrbitSwitch
import feature.home.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmSoundBottomSheet(
    vibrationEnabled: Boolean,
    soundEnabled: Boolean,
    soundVolume: Int,
    soundIndex: Int,
    sounds: List<AlarmSound>,
    onVibrationToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onVolumeChanged: (Int) -> Unit,
    onSoundSelected: (Int) -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    OrbitBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        },
    ) {
        BottomSheetContent(
            vibrationEnabled = vibrationEnabled,
            soundEnabled = soundEnabled,
            soundVolume = soundVolume,
            soundIndex = soundIndex,
            sounds = sounds,
            onVibrationToggle = onVibrationToggle,
            onSoundToggle = onSoundToggle,
            onVolumeChanged = onVolumeChanged,
            onSoundSelected = onSoundSelected,
            onComplete = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion { onComplete() }
            },
        )
    }
}

@Composable
private fun BottomSheetContent(
    vibrationEnabled: Boolean,
    soundEnabled: Boolean,
    soundVolume: Int,
    soundIndex: Int,
    sounds: List<AlarmSound>,
    onVibrationToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onVolumeChanged: (Int) -> Unit,
    onSoundSelected: (Int) -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        VibrationSection(
            isVibrationEnabled = vibrationEnabled,
            onVibrationToggle = onVibrationToggle,
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = OrbitTheme.colors.gray_700),
        )
        SoundSection(
            modifier = Modifier.weight(1f),
            soundEnabled = soundEnabled,
            onSoundToggle = onSoundToggle,
            soundVolume = soundVolume,
            onVolumeChanged = onVolumeChanged,
            soundIndex = soundIndex,
            sounds = sounds,
            onSoundSelected = { onSoundSelected(it) },
        )

        OrbitButton(
            label = stringResource(id = R.string.alarm_add_edit_complete),
            enabled = true,
            containerColor = OrbitTheme.colors.gray_600,
            contentColor = OrbitTheme.colors.white,
            pressedContainerColor = OrbitTheme.colors.gray_500,
            pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
            onClick = onComplete,
        )
    }
}

@Composable
private fun VibrationSection(
    isVibrationEnabled: Boolean,
    onVibrationToggle: () -> Unit,
) {
    Column {
        Text(
            text = stringResource(id = R.string.alarm_add_edit_sound),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.alarm_add_edit_vibration),
                style = OrbitTheme.typography.headline2Medium,
                color = OrbitTheme.colors.gray_50,
            )
            Spacer(modifier = Modifier.weight(1f))
            OrbitSwitch(
                isChecked = isVibrationEnabled,
                isEnabled = true,
                onClick = onVibrationToggle,
            )
        }
    }
}

@Composable
private fun SoundSection(
    modifier: Modifier = Modifier,
    soundEnabled: Boolean,
    onSoundToggle: () -> Unit,
    soundVolume: Int,
    onVolumeChanged: (Int) -> Unit,
    sounds: List<AlarmSound>,
    soundIndex: Int,
    onSoundSelected: (Int) -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.alarm_add_edit_ringtone),
                style = OrbitTheme.typography.headline2Medium,
                color = OrbitTheme.colors.gray_50,
            )
            Spacer(modifier = Modifier.weight(1f))
            OrbitSwitch(
                isChecked = soundEnabled,
                isEnabled = true,
                onClick = onSoundToggle,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = core.designsystem.R.drawable.ic_sound_volume),
                contentDescription = "Volume",
                tint = OrbitTheme.colors.gray_400,
            )
            Spacer(modifier = Modifier.width(8.dp))
            OrbitSlider(
                enabled = soundEnabled,
                value = soundVolume,
                onValueChange = onVolumeChanged,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SoundSelectionSection(
            soundEnabled = soundEnabled,
            soundIndex = soundIndex,
            sounds = sounds,
            onSoundSelected = { onSoundSelected(it) },
        )
    }
}

@Composable
private fun SoundSelectionSection(
    modifier: Modifier = Modifier,
    soundEnabled: Boolean,
    soundIndex: Int,
    sounds: List<AlarmSound>,
    onSoundSelected: (Int) -> Unit,
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        scrollState.animateScrollToItem(soundIndex)
    }

    LazyColumn(
        modifier = modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 20.dp),
        state = scrollState,
    ) {
        items(sounds.size) { index ->
            SoundSelectionItem(
                sound = sounds[index],
                enabled = soundEnabled,
                selected = index == soundIndex,
                onClick = { onSoundSelected(index) },
            )
            if (index != sounds.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SoundSelectionItem(
    sound: AlarmSound,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrbitRadioButton(
            enabled = enabled,
            selected = selected,
            onClick = onClick,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = sound.title,
            style = OrbitTheme.typography.body1Medium,
            color = OrbitTheme.colors.white,
        )
    }
}

@Preview
@Composable
private fun AlarmSoundBottomSheetPreview() {
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var soundVolume by remember { mutableIntStateOf(0) }
    var soundIndex by remember { mutableIntStateOf(0) }
    val sounds by remember { mutableStateOf((1..20).map { AlarmSound("sound $it", Uri.EMPTY) }) }
    var isSheetOpen by remember { mutableStateOf(true) }

    OrbitTheme {
        if (isSheetOpen) {
            AlarmSoundBottomSheet(
                vibrationEnabled = isVibrationEnabled,
                soundEnabled = isSoundEnabled,
                soundVolume = soundVolume,
                soundIndex = soundIndex,
                sounds = sounds,
                onVibrationToggle = { isVibrationEnabled = !isVibrationEnabled },
                onSoundToggle = { isSoundEnabled = !isSoundEnabled },
                onVolumeChanged = { soundVolume = it },
                onSoundSelected = { soundIndex = it },
                onComplete = { isSheetOpen = false },
                onDismiss = { isSheetOpen = false },
            )
        }
    }
}
