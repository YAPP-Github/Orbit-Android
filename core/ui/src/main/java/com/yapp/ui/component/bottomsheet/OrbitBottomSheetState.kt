package com.yapp.ui.component.bottomsheet

import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberOrbitBottomSheetState(
    bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
    ),
): OrbitBottomSheetState {
    return remember(bottomSheetState) { OrbitBottomSheetState(state = bottomSheetState) }
}

class OrbitBottomSheetState(
    val state: ModalBottomSheetState,
) {
    var content by mutableStateOf<BottomSheetContent?>(null)
        private set

    suspend fun show(sheetContent: BottomSheetContent) {
        content = sheetContent
        state.show()
    }

    suspend fun hide() = state.hide()
}
