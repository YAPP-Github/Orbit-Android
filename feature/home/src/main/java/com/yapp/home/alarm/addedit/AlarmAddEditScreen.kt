package com.yapp.home.alarm.addedit

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.MissionType
import com.yapp.home.ADD_ALARM_RESULT_KEY
import com.yapp.home.DELETE_ALARM_RESULT_KEY
import com.yapp.home.UPDATE_ALARM_RESULT_KEY
import com.yapp.home.alarm.component.AlarmCheckItem
import com.yapp.home.alarm.component.AlarmDayButton
import com.yapp.home.alarm.component.bottomsheet.AlarmMissionBottomSheet
import com.yapp.home.alarm.component.bottomsheet.AlarmSnoozeBottomSheet
import com.yapp.home.alarm.component.bottomsheet.AlarmSoundBottomSheet
import com.yapp.home.alarm.getLabelStringRes
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.component.snackbar.showCustomSnackBar
import com.yapp.ui.component.switch.OrbitSwitch
import com.yapp.ui.component.timepicker.OrbitPicker
import feature.home.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalTime

@Composable
fun AlarmAddEditRoute(
    viewModel: AlarmAddEditViewModel = hiltViewModel(),
    navigator: OrbitNavigator,
    snackBarHostState: SnackbarHostState,
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    viewModel.collectSideEffect {
        handleSideEffect(it, navigator, snackBarHostState, coroutineScope)
    }

    AlarmAddEditScreen(
        stateProvider = { state },
        eventDispatcher = viewModel::processAction,
    )
}

private suspend fun handleSideEffect(
    sideEffect: AlarmAddEditContract.SideEffect,
    navigator: OrbitNavigator,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    when (sideEffect) {
        is AlarmAddEditContract.SideEffect.NavigateBack -> {
            navigator.navigateBack()
        }
        is AlarmAddEditContract.SideEffect.NavigateToMissionPreview -> {
            navigator.navigateToMissionPreview(
                missionType = sideEffect.missionType.value,
                missionCount = sideEffect.missionCount,
            )
        }
        is AlarmAddEditContract.SideEffect.SaveAlarm -> {
            navigator.navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(ADD_ALARM_RESULT_KEY, sideEffect.id)
            navigator.navController.popBackStack()
        }
        is AlarmAddEditContract.SideEffect.UpdateAlarm -> {
            navigator.navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(UPDATE_ALARM_RESULT_KEY, sideEffect.id)
            navigator.navigateBack()
        }
        is AlarmAddEditContract.SideEffect.DeleteAlarm -> {
            navigator.navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(DELETE_ALARM_RESULT_KEY, sideEffect.id)
            navigator.navigateBack()
        }
        is AlarmAddEditContract.SideEffect.ShowSnackBar -> {
            val result = showCustomSnackBar(
                scope = coroutineScope,
                snackBarHostState = snackBarHostState,
                message = sideEffect.message,
                actionLabel = sideEffect.label,
                iconRes = sideEffect.iconRes,
                bottomPadding = sideEffect.bottomPadding,
                durationMillis = sideEffect.durationMillis,
            )

            when (result) {
                SnackbarResult.ActionPerformed -> sideEffect.onAction()
                SnackbarResult.Dismissed -> sideEffect.onDismiss()
            }
        }
    }
}

@Composable
fun AlarmAddEditScreen(
    stateProvider: () -> AlarmAddEditContract.State,
    eventDispatcher: (AlarmAddEditContract.Action) -> Unit,
) {
    val state = stateProvider()

    if (state.initialLoading) {
        AlarmAddEditLoadingScreen()
    } else {
        AlarmAddEditContent(
            state = state,
            eventDispatcher = eventDispatcher,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmAddEditContent(
    state: AlarmAddEditContract.State,
    eventDispatcher: (AlarmAddEditContract.Action) -> Unit,
) {
    BackHandler {
        eventDispatcher(AlarmAddEditContract.Action.CheckUnsavedChangesBeforeExit)
    }

    val missionState = state.missionState
    val snoozeState = state.snoozeState
    val missionBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snoozeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val soundBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlarmAddEditTopBar(
            mode = state.mode,
            title = state.timeState.alarmMessage,
            onBack = { eventDispatcher(AlarmAddEditContract.Action.CheckUnsavedChangesBeforeExit) },
            onDelete = { eventDispatcher(AlarmAddEditContract.Action.ShowDeleteDialog) },
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            OrbitPicker(
                initialTime = state.timeState.initialTime,
            ) { newTime ->
                eventDispatcher(AlarmAddEditContract.Action.SetAlarmTime(newTime))
            }
        }
        AlarmAddEditSelectDaysSection(
            modifier = Modifier.padding(horizontal = 20.dp),
            daysSelectionState = state.daySelectionState,
            holidayState = state.holidayState,
            processAction = eventDispatcher,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AlarmAddEditSettingsSection(
            modifier = Modifier.padding(horizontal = 20.dp),
            state = state,
            processAction = eventDispatcher,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OrbitButton(
            label = stringResource(R.string.alarm_add_edit_save),
            onClick = { eventDispatcher(AlarmAddEditContract.Action.SaveAlarm) },
            enabled = true,
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp,
                ),
        )
    }

    when (state.bottomSheetState) {
        AlarmAddEditContract.BottomSheetType.MissionSetting -> {
            AlarmMissionBottomSheet(
                sheetState = missionBottomSheetState,
                missionType = missionState.missionType,
                missionCount = missionState.missionCount,
                onDismiss = {
                    scope.launch {
                        missionBottomSheetState.hide()
                    }.invokeOnCompletion {
                        eventDispatcher(
                            AlarmAddEditContract.Action.ToggleBottomSheet(
                                AlarmAddEditContract.BottomSheetType.MissionSetting,
                            ),
                        )
                    }
                },
                onSaveMission = { missionType, missionCount ->
                    eventDispatcher(
                        AlarmAddEditContract.Action.SaveMission(
                            type = missionType,
                            count = missionCount,
                        ),
                    )
                },
                onPreviewMission = { missionType, missionCount ->
                    eventDispatcher(
                        AlarmAddEditContract.Action.NavigateToMissionPreview(
                            missionType = missionType,
                            missionCount = missionCount,
                        ),
                    )
                },
            )
        }

        AlarmAddEditContract.BottomSheetType.SnoozeSetting -> {
            AlarmSnoozeBottomSheet(
                snoozeEnabled = snoozeState.isSnoozeEnabled,
                snoozeInterval = snoozeState.snoozeInterval,
                snoozeCount = snoozeState.snoozeCount,
                onSnoozeToggle = { eventDispatcher(AlarmAddEditContract.Action.ToggleSnoozeOption) },
                onIntervalSelected = { interval ->
                    eventDispatcher(
                        AlarmAddEditContract.Action.SetSnoozeInterval(interval),
                    )
                },
                onCountSelected = { count ->
                    eventDispatcher(
                        AlarmAddEditContract.Action.SetSnoozeRepeatCount(count),
                    )
                },
                onDismiss = {
                    scope.launch {
                        snoozeBottomSheetState.hide()
                    }.invokeOnCompletion {
                        eventDispatcher(
                            AlarmAddEditContract.Action.ToggleBottomSheet(
                                AlarmAddEditContract.BottomSheetType.SnoozeSetting,
                            ),
                        )
                    }
                },
            )
        }

        AlarmAddEditContract.BottomSheetType.SoundSetting -> {
            AlarmSoundBottomSheet(
                vibrationEnabled = state.soundState.isVibrationEnabled,
                soundEnabled = state.soundState.isSoundEnabled,
                soundVolume = state.soundState.soundVolume,
                soundIndex = state.soundState.soundIndex,
                sounds = state.soundState.sounds,
                onVibrationToggle = { eventDispatcher(AlarmAddEditContract.Action.ToggleVibrationOption) },
                onSoundToggle = { eventDispatcher(AlarmAddEditContract.Action.ToggleSoundOption) },
                onVolumeChanged = { eventDispatcher(AlarmAddEditContract.Action.AdjustSoundVolume(it)) },
                onSoundSelected = { eventDispatcher(AlarmAddEditContract.Action.SelectAlarmSound(it)) },
                onComplete = {
                    scope.launch {
                        soundBottomSheetState.hide()
                    }.invokeOnCompletion {
                        eventDispatcher(
                            AlarmAddEditContract.Action.ToggleBottomSheet(
                                AlarmAddEditContract.BottomSheetType.SoundSetting,
                            ),
                        )
                    }
                },
                onDismiss = {
                    scope.launch {
                        soundBottomSheetState.hide()
                    }.invokeOnCompletion {
                        eventDispatcher(
                            AlarmAddEditContract.Action.ToggleBottomSheet(
                                AlarmAddEditContract.BottomSheetType.SoundSetting,
                            ),
                        )
                    }
                },
            )
        }

        else -> null
    }

    if (state.isDeleteDialogVisible) {
        OrbitDialog(
            title = stringResource(id = R.string.alarm_delete_dialog_title),
            message = stringResource(id = R.string.alarm_delete_dialog_message),
            confirmText = stringResource(id = R.string.alarm_delete_dialog_btn_delete),
            cancelText = stringResource(id = R.string.alarm_delete_dialog_btn_cancel),
            onConfirm = {
                eventDispatcher(AlarmAddEditContract.Action.DeleteAlarm)
            },
            onCancel = {
                eventDispatcher(AlarmAddEditContract.Action.HideDeleteDialog)
            },
        )
    }

    if (state.isUnsavedChangesDialogVisible) {
        OrbitDialog(
            title = stringResource(id = R.string.alarm_unsaved_changes_dialog_title),
            message = stringResource(id = R.string.alarm_unsaved_changes_dialog_message),
            confirmText = stringResource(id = R.string.alarm_unsaved_changes_dialog_btn_discard),
            cancelText = stringResource(id = R.string.alarm_unsaved_changes_dialog_btn_cancel),
            onConfirm = {
                eventDispatcher(AlarmAddEditContract.Action.NavigateBack)
            },
            onCancel = {
                eventDispatcher(AlarmAddEditContract.Action.HideUnsavedChangesDialog)
            },
        )
    }
}

@Composable
private fun AlarmAddEditLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.Center),
            resId = core.designsystem.R.raw.star_loading,
        )
    }
}

@Composable
private fun AlarmAddEditTopBar(
    mode: AlarmAddEditContract.EditMode = AlarmAddEditContract.EditMode.ADD,
    title: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = core.designsystem.R.drawable.ic_back),
            contentDescription = "Back",
            tint = OrbitTheme.colors.white,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
        )

        Text(
            title,
            style = OrbitTheme.typography.body1SemiBold,
            color = OrbitTheme.colors.white,
        )

        if (mode == AlarmAddEditContract.EditMode.EDIT) {
            DeleteAlarmButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
            ) {
                onDelete()
            }
        }
    }
}

@Composable
private fun DeleteAlarmButton(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    Surface(
        onClick = onDelete,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        interactionSource = interactionSource,
        color = if (isPressed) OrbitTheme.colors.gray_800 else Color.Transparent,
    ) {
        Text(
            text = stringResource(id = R.string.alarm_add_edit_delete),
            style = OrbitTheme.typography.body1Medium,
            color = OrbitTheme.colors.alert,
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp,
                ),
        )
    }
}

@Composable
private fun AlarmAddEditSettingsSection(
    modifier: Modifier = Modifier,
    state: AlarmAddEditContract.State,
    processAction: (AlarmAddEditContract.Action) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = OrbitTheme.colors.gray_800,
                shape = RoundedCornerShape(12.dp),
            )
            .clip(
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        AlarmAddEditSettingItem(
            label = stringResource(id = R.string.alarm_add_edit_mission),
            description = when (state.missionState.missionType) {
                MissionType.TAP -> {
                    val missionType = stringResource(id = R.string.alarm_add_edit_selected_mission_tap)
                    val missionCount = state.missionState.missionCount
                    stringResource(
                        id = R.string.alarm_add_edit_selected_mission_with_count,
                        missionType,
                        missionCount,
                    )
                }
                MissionType.SHAKE -> {
                    val missionType = stringResource(id = R.string.alarm_add_edit_selected_mission_shake)
                    val missionCount = state.missionState.missionCount
                    stringResource(
                        id = R.string.alarm_add_edit_selected_mission_with_count,
                        missionType,
                        missionCount,
                    )
                }
                else -> stringResource(id = R.string.alarm_add_edit_selected_mission_none)
            },
            onClick = {
                processAction(
                    AlarmAddEditContract.Action.ToggleBottomSheet(
                        AlarmAddEditContract.BottomSheetType.MissionSetting,
                    ),
                )
            },
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 20.dp)
                .background(OrbitTheme.colors.gray_700),
        )
        AlarmAddEditSettingItem(
            label = stringResource(id = R.string.alarm_add_edit_alarm_snooze),
            description = if (state.snoozeState.isSnoozeEnabled) {
                val interval = stringResource(
                    id = R.string.alarm_add_edit_interval_minute,
                    state.snoozeState.snoozeInterval,
                )
                val count = if (state.snoozeState.snoozeCount == -1) {
                    stringResource(id = R.string.alarm_add_edit_repeat_count_infinite)
                } else {
                    stringResource(
                        id = R.string.alarm_add_edit_repeat_count_times,
                        state.snoozeState.snoozeCount,
                    )
                }
                stringResource(
                    id = R.string.alarm_add_edit_alarm_selected_option,
                    interval,
                    count,
                )
            } else {
                stringResource(id = R.string.alarm_add_edit_alarm_selected_option_none)
            },
            onClick = {
                processAction(
                    AlarmAddEditContract.Action.ToggleBottomSheet(
                        AlarmAddEditContract.BottomSheetType.SnoozeSetting,
                    ),
                )
            },
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 20.dp)
                .background(OrbitTheme.colors.gray_700),
        )
        AlarmAddEditSettingItem(
            label = stringResource(id = R.string.alarm_add_edit_sound),
            description = when {
                state.soundState.isSoundEnabled && state.soundState.isVibrationEnabled -> {
                    "${stringResource(id = R.string.alarm_add_edit_vibration)}, ${
                    state.soundState.sounds.getOrElse(state.soundState.soundIndex) {
                        AlarmSound("", Uri.EMPTY)
                    }.title
                    }"
                }

                state.soundState.isSoundEnabled -> state.soundState.sounds.getOrElse(state.soundState.soundIndex) {
                    AlarmSound(
                        "",
                        Uri.EMPTY,
                    )
                }.title

                state.soundState.isVibrationEnabled -> stringResource(id = R.string.alarm_add_edit_vibration)
                else -> stringResource(id = R.string.alarm_add_edit_alarm_selected_option_none)
            },
            onClick = {
                processAction(
                    AlarmAddEditContract.Action.ToggleBottomSheet(
                        AlarmAddEditContract.BottomSheetType.SoundSetting,
                    ),
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmAddEditSettingItem(
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            rippleAlpha = RippleAlpha(
                pressedAlpha = 1f,
                focusedAlpha = 1f,
                hoveredAlpha = 1f,
                draggedAlpha = 1f,
            ),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(
                        color = OrbitTheme.colors.gray_700,
                    ),
                ) {
                    onClick()
                }
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                modifier = Modifier.width(80.dp),
                style = OrbitTheme.typography.body1SemiBold,
                color = OrbitTheme.colors.white,
            )
            Text(
                description,
                modifier = Modifier.weight(1f),
                style = OrbitTheme.typography.body2Regular,
                color = OrbitTheme.colors.gray_50,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
            Icon(
                painter = painterResource(id = core.designsystem.R.drawable.ic_arrow_right),
                contentDescription = "Arrow",
                tint = OrbitTheme.colors.gray_300,
            )
        }
    }
}

@Composable
private fun AlarmAddEditSelectDaysSection(
    modifier: Modifier = Modifier,
    daysSelectionState: AlarmAddEditContract.AlarmDaySelectionState,
    holidayState: AlarmAddEditContract.AlarmHolidayState,
    processAction: (AlarmAddEditContract.Action) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = OrbitTheme.colors.gray_800,
                shape = RoundedCornerShape(12.dp),
            )
            .clip(
                shape = RoundedCornerShape(12.dp),
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.alarm_add_edit_repeat),
                    style = OrbitTheme.typography.body1SemiBold,
                    color = OrbitTheme.colors.white,
                )

                Spacer(modifier = Modifier.weight(1f))

                AlarmCheckItem(
                    label = stringResource(id = R.string.alarm_add_edit_weekdays),
                    isPressed = daysSelectionState.isWeekdaysChecked,
                    onClick = {
                        processAction(AlarmAddEditContract.Action.ToggleWeekdaysSelection)
                    },
                )
                Spacer(modifier = Modifier.width(2.dp))
                AlarmCheckItem(
                    label = stringResource(id = R.string.alarm_add_edit_weekends),
                    isPressed = daysSelectionState.isWeekendsChecked,
                    onClick = {
                        processAction(AlarmAddEditContract.Action.ToggleWeekendsSelection)
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                daysSelectionState.days.forEach { day ->
                    AlarmDayButton(
                        modifier = Modifier.size(
                            if (screenWidthDp > 360.dp) 36.dp else 34.dp,
                        ),
                        label = stringResource(id = day.getLabelStringRes()),
                        isPressed = daysSelectionState.selectedDays.contains(day),
                        onClick = {
                            processAction(AlarmAddEditContract.Action.ToggleSpecificDaySelection(day))
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AlarmAddEditDisableHolidaySwitch(
                state = holidayState,
                processAction = processAction,
            )
        }
    }
}

@Composable
private fun AlarmAddEditDisableHolidaySwitch(
    modifier: Modifier = Modifier,
    state: AlarmAddEditContract.AlarmHolidayState,
    processAction: (AlarmAddEditContract.Action) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = core.designsystem.R.drawable.ic_holiday),
            contentDescription = "Holiday",
            tint = OrbitTheme.colors.gray_400,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = stringResource(id = R.string.alarm_add_edit_disable_holiday),
            style = OrbitTheme.typography.label1Medium,
            color = OrbitTheme.colors.gray_400,
        )

        Spacer(modifier = Modifier.weight(1f))

        OrbitSwitch(
            isChecked = state.isDisableHolidayChecked,
            isEnabled = state.isDisableHolidayEnabled,
            onClick = {
                processAction(AlarmAddEditContract.Action.ToggleHolidaySkipOption)
            },
        )
    }
}

@Preview
@Composable
fun AlarmAddEditSettingsSectionPreview() {
    AlarmAddEditSettingsSection(
        state = AlarmAddEditContract.State(
            timeState = AlarmAddEditContract.AlarmTimeState(
                currentTime = LocalTime.of(19, 30),
            ),
            daySelectionState = AlarmAddEditContract.AlarmDaySelectionState(
                isWeekdaysChecked = true,
                isWeekendsChecked = false,
                selectedDays = setOf(AlarmDay.MON, AlarmDay.TUE),
                days = AlarmDay.entries.toSet(),
            ),
            holidayState = AlarmAddEditContract.AlarmHolidayState(
                isDisableHolidayChecked = false,
            ),
        ),
        processAction = { },
    )
}

@Preview
@Composable
fun AlarmAddEditSettingItemPreview() {
    AlarmAddEditSettingItem(
        label = "알람 미루기",
        description = "5분, 무한",
        onClick = { },
    )
}

@Preview
@Composable
fun AlarmAddEditScreenPreview() {
    OrbitTheme {
        Box(
            modifier = Modifier.background(
                color = OrbitTheme.colors.gray_900,
            ),
        ) {
            AlarmAddEditScreen(
                stateProvider = {
                    AlarmAddEditContract.State(
                        initialLoading = false,
                        timeState = AlarmAddEditContract.AlarmTimeState(
                            currentTime = LocalTime.of(19, 30),
                        ),
                        daySelectionState = AlarmAddEditContract.AlarmDaySelectionState(
                            isWeekdaysChecked = true,
                            isWeekendsChecked = false,
                            selectedDays = setOf(AlarmDay.MON, AlarmDay.TUE),
                            days = AlarmDay.entries.toSet(),
                        ),
                        holidayState = AlarmAddEditContract.AlarmHolidayState(
                            isDisableHolidayChecked = false,
                        ),
                    )
                },
                eventDispatcher = { },
            )
        }
    }
}
