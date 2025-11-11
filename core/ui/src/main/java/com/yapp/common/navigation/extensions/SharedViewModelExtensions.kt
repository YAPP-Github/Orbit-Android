package com.yapp.common.navigation.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedHiltViewModel(
    navController: NavController,
): T {
    val navGraphRoute = destination.parent?.route

    return if (navGraphRoute != null) {
        val parentEntry = remember(this) {
            navController.getBackStackEntry(navGraphRoute)
        }
        hiltViewModel(parentEntry)
    } else {
        hiltViewModel()
    }
}
