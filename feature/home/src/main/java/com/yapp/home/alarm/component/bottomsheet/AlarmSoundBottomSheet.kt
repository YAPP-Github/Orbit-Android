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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.AlarmSound
import com.yapp.home.alarm.addedit.AlarmAddEditContract
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.radiobutton.OrbitRadioButton
import com.yapp.ui.component.slider.OrbitSlider
import com.yapp.ui.component.switch.OrbitSwitch
import feature.home.R

@Composable
internal fun AlarmSoundBottomSheet(
    soundState: AlarmAddEditContract.AlarmSoundState,
    onVibrationToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onVolumeChanged: (Int) -> Unit,
    onSoundSelected: (Int) -> Unit,
    onDismiss: () -> Unit = {},
    onComplete: (vibrationEnabled: Boolean, soundEnabled: Boolean, soundVolume: Int, soundIndex: Int) -> Unit,
) {
    var selectedVibrationEnabled by remember { mutableStateOf(soundState.isVibrationEnabled) }
    var selectedSoundEnabled by remember { mutableStateOf(soundState.isSoundEnabled) }
    var selectedSoundVolume by remember { mutableIntStateOf(soundState.soundVolume) }
    var selectedSoundIndex by remember { mutableIntStateOf(soundState.soundIndex) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        VibrationSection(
            isVibrationEnabled = selectedVibrationEnabled,
            onVibrationToggle = {
                selectedVibrationEnabled = !selectedVibrationEnabled
                onVibrationToggle(selectedVibrationEnabled)
            },
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = OrbitTheme.colors.gray_700),
        )
        SoundSection(
            modifier = Modifier.weight(1f),
            soundEnabled = selectedSoundEnabled,
            onSoundToggle = {
                selectedSoundEnabled = !selectedSoundEnabled
                onSoundToggle(selectedSoundEnabled)
            },
            soundVolume = selectedSoundVolume,
            onVolumeChanged = {
                selectedSoundVolume = it
                onVolumeChanged(it)
            },
            soundIndex = selectedSoundIndex,
            sounds = soundState.sounds,
            onSoundSelected = {
                selectedSoundIndex = it
                onSoundSelected(it)
            },
        )

        OrbitButton(
            label = stringResource(id = R.string.alarm_add_edit_complete),
            enabled = true,
            containerColor = OrbitTheme.colors.gray_600,
            contentColor = OrbitTheme.colors.white,
            pressedContainerColor = OrbitTheme.colors.gray_500,
            pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
            onClick = {
                onDismiss()
                onComplete(
                    selectedVibrationEnabled,
                    selectedSoundEnabled,
                    selectedSoundVolume,
                    selectedSoundIndex,
                )
            },
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
    OrbitTheme {
        AlarmSoundBottomSheet(
            soundState = AlarmAddEditContract.AlarmSoundState(
                sounds = (1..20).map { AlarmSound("sound $it", Uri.EMPTY) },
            ),
            onVibrationToggle = {},
            onSoundToggle = {},
            onVolumeChanged = {},
            onSoundSelected = {},
            onComplete = { _, _, _, _ ->
            },
        )
    }
}
