package com.example.hop.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class OtherProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val reviews: List<UserReview> = emptyList(),
    val carDetails: CarDetails? = null,
    val isReportDialogVisible: Boolean = false,
    val isSubmittingReport: Boolean = false,
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface OtherProfileEvent {
    data class Load(val userId: String) : OtherProfileEvent
    data object ShowReportDialog : OtherProfileEvent
    data class SubmitReport(val reason: String) : OtherProfileEvent
    data object DismissReportDialog : OtherProfileEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface OtherProfileEffect {
    data class ShowSnackbar(val message: String) : OtherProfileEffect
    data object NavigateBack : OtherProfileEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class OtherProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OtherProfileUiState())
    val state: StateFlow<OtherProfileUiState> = _state.asStateFlow()

    private val _effect = Channel<OtherProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: OtherProfileEvent) {
        when (event) {
            is OtherProfileEvent.Load -> load(event.userId)
            is OtherProfileEvent.ShowReportDialog -> _state.value = _state.value.copy(isReportDialogVisible = true)
            is OtherProfileEvent.DismissReportDialog -> _state.value = _state.value.copy(isReportDialogVisible = false)
            is OtherProfileEvent.SubmitReport -> submitReport(event.reason)
        }
    }

    private fun load(userId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val profileResponse = userRepository.getUserProfile(userId)

            if (profileResponse is ApiResponse.Error) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = profileResponse.message,
                )
                return@launch
            }

            val user = (profileResponse as ApiResponse.Success).data

            val reviewsDeferred = async { userRepository.getUserReviews(user.id) }
            val carDeferred = async { userRepository.getUserCarDetails(user.id) }

            val reviewsResponse = reviewsDeferred.await()
            val carResponse = carDeferred.await()

            _state.value = _state.value.copy(
                isLoading = false,
                user = user,
                reviews = (reviewsResponse as? ApiResponse.Success)?.data.orEmpty(),
                carDetails = (carResponse as? ApiResponse.Success)?.data,
            )
        }
    }

    private fun submitReport(reason: String) {
        val userId = _state.value.user?.id ?: return
        if (_state.value.isSubmittingReport) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmittingReport = true)
            when (val response = userRepository.reportUser(userId, reason)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isSubmittingReport = false,
                        isReportDialogVisible = false,
                    )
                    _effect.send(OtherProfileEffect.ShowSnackbar("Report submitted. Thank you."))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isSubmittingReport = false)
                    _effect.send(OtherProfileEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
