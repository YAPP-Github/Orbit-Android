package com.yapp.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yapp.domain.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel(), ContainerHost<SettingContract.State, SettingContract.SideEffect> {

    override val container: Container<SettingContract.State, SettingContract.SideEffect> = container(
        initialState = SettingContract.State(),
    ) {
        intent {
            repeatOnSubscription {
                refreshUserInfo()
            }
        }
    }

    fun processAction(action: SettingContract.Action) = intent {
        when (action) {
            SettingContract.Action.PreviousStep -> navigateBack()
            SettingContract.Action.NavigateToEditProfile -> navigateToEditProfile()
            is SettingContract.Action.OpenWebView -> openWebView(action.url)
            SettingContract.Action.RefreshUserInfo -> refreshUserInfo()
            else -> {}
        }
    }

    private fun fetchUserInfo(userId: Long) = intent {
        userInfoRepository.getUserInfo(userId)
            .onSuccess { user ->
                reduce {
                    state.copy(
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

    private fun navigateBack() = intent {
        postSideEffect(SettingContract.SideEffect.NavigateBack)
    }

    private fun navigateToEditProfile() = intent {
        postSideEffect(SettingContract.SideEffect.NavigateToEditProfile)
    }

    private fun openWebView(url: String) = intent {
        postSideEffect(SettingContract.SideEffect.OpenWebView(url))
    }

    private fun refreshUserInfo() = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull()
        if (userId != null) {
            fetchUserInfo(userId)
        }
    }
}
