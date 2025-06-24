package com.yapp.setting

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
) : BaseViewModel<SettingContract.State, SettingContract.SideEffect>(
    SettingContract.State(),
) {
    fun onAction(action: SettingContract.Action) = intent {
        when (action) {
            SettingContract.Action.PreviousStep -> emitSideEffect(SettingContract.SideEffect.NavigateBack)
            SettingContract.Action.NavigateToEditProfile -> navigateToEditProfile()
            is SettingContract.Action.OpenWebView -> openWebView(action.url)
            SettingContract.Action.RefreshUserInfo -> refreshUserInfo()
            else -> {}
        }
    }

    private fun fetchUserInfo(userId: Long) {
        viewModelScope.launch {
            userInfoRepository.getUserInfo(userId)
                .onSuccess { user ->
                    updateState {
                        copy(
                            initialLoading = false,
                            name = user.name,
                            birthDate = user.birthDate,
                            selectedGender = user.gender,
                            timeOfBirth = user.birthTime.toString(),
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("SettingViewModel", "사용자 정보 가져오기 실패: ${error.message}")
                }
        }
    }

    private fun navigateToEditProfile() {
        emitSideEffect(SettingContract.SideEffect.NavigateToEditProfile)
    }

    private fun openWebView(url: String) {
        emitSideEffect(SettingContract.SideEffect.OpenWebView(url))
    }

    private fun refreshUserInfo() {
        viewModelScope.launch {
            val userId = userInfoRepository.userIdFlow.firstOrNull()
            if (userId != null) {
                fetchUserInfo(userId)
            }
        }
    }
}
