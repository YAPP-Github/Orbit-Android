package com.yapp.ui.component.bottomsheet

import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberOrbitBottomSheetState(): OrbitBottomSheetState {
    val contentState = remember { mutableStateOf<BottomSheetContent?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { value ->
            if (value == ModalBottomSheetValue.Hidden) {
                contentState.value = null
            }
            true
        },
        skipHalfExpanded = true,
    )

    return remember(contentState, bottomSheetState) {
        OrbitBottomSheetState(
            state = bottomSheetState,
            contentState = contentState,
            setContent = { contentState.value = it },
        )
    }
}

class OrbitBottomSheetState(
    val state: ModalBottomSheetState,
    val contentState: State<BottomSheetContent?>,
    val setContent: (BottomSheetContent?) -> Unit,
) {
    val content: BottomSheetContent?
        get() = contentState.value

    suspend fun show(sheetContent: BottomSheetContent) {
        setContent(sheetContent)
        state.show()
    }

    suspend fun hide() = state.hide()
}
