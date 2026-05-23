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

data class OwnProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val reviews: List<UserReview> = emptyList(),
    val carDetails: CarDetails? = null,
    val isEditingName: Boolean = false,
    val nameDraft: String = "",
    val isSavingName: Boolean = false,
    val isEditingMobilepay: Boolean = false,
    val mobilepayDraft: String = "",
    val isSavingMobilepay: Boolean = false,
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface OwnProfileEvent {
    data object Load : OwnProfileEvent
    data object StartEditName : OwnProfileEvent
    data class NameDraftChanged(val name: String) : OwnProfileEvent
    data object SaveName : OwnProfileEvent
    data object CancelEditName : OwnProfileEvent
    data object AddPhoneTapped : OwnProfileEvent
    data object EditCarTapped : OwnProfileEvent
    data object StartEditMobilepay : OwnProfileEvent
    data class MobilepayDraftChanged(val number: String) : OwnProfileEvent
    data object SaveMobilepay : OwnProfileEvent
    data object CancelEditMobilepay : OwnProfileEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface OwnProfileEffect {
    data object NavigateToPhoneVerification : OwnProfileEffect
    data object NavigateToEditCar : OwnProfileEffect
    data class ShowSnackbar(val message: String) : OwnProfileEffect
    data object NavigateBack : OwnProfileEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class OwnProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OwnProfileUiState())
    val state: StateFlow<OwnProfileUiState> = _state.asStateFlow()

    private val _effect = Channel<OwnProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: OwnProfileEvent) {
        when (event) {
            is OwnProfileEvent.Load -> load()
            is OwnProfileEvent.StartEditName -> startEditName()
            is OwnProfileEvent.NameDraftChanged -> _state.value = _state.value.copy(nameDraft = event.name)
            is OwnProfileEvent.SaveName -> saveName()
            is OwnProfileEvent.CancelEditName -> _state.value = _state.value.copy(
                isEditingName = false,
                nameDraft = _state.value.user?.fullName.orEmpty(),
            )
            is OwnProfileEvent.AddPhoneTapped -> viewModelScope.launch {
                _effect.send(OwnProfileEffect.NavigateToPhoneVerification)
            }
            is OwnProfileEvent.EditCarTapped -> viewModelScope.launch {
                _effect.send(OwnProfileEffect.NavigateToEditCar)
            }
            is OwnProfileEvent.StartEditMobilepay -> _state.value = _state.value.copy(
                isEditingMobilepay = true,
                mobilepayDraft = _state.value.user?.mobilepayNumber.orEmpty(),
            )
            is OwnProfileEvent.MobilepayDraftChanged -> _state.value = _state.value.copy(mobilepayDraft = event.number)
            is OwnProfileEvent.SaveMobilepay -> saveMobilepay()
            is OwnProfileEvent.CancelEditMobilepay -> _state.value = _state.value.copy(
                isEditingMobilepay = false,
                mobilepayDraft = _state.value.user?.mobilepayNumber.orEmpty(),
            )
        }
    }

    private fun load() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val profileDeferred = async { userRepository.getCurrentUserProfile() }
            val profileResponse = profileDeferred.await()

            if (profileResponse is ApiResponse.Error) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = profileResponse.message,
                )
                return@launch
            }

            val user = (profileResponse as ApiResponse.Success).data

            // Fetch reviews and car details concurrently
            val reviewsDeferred = async { userRepository.getUserReviews(user.id) }
            val carDeferred = async { userRepository.getUserCarDetails(user.id) }

            val reviewsResponse = reviewsDeferred.await()
            val carResponse = carDeferred.await()

            _state.value = _state.value.copy(
                isLoading = false,
                user = user,
                nameDraft = user.fullName,
                reviews = (reviewsResponse as? ApiResponse.Success)?.data.orEmpty(),
                carDetails = (carResponse as? ApiResponse.Success)?.data,
            )
        }
    }

    private fun startEditName() {
        val currentName = _state.value.user?.fullName.orEmpty()
        _state.value = _state.value.copy(
            isEditingName = true,
            nameDraft = currentName,
        )
    }

    private fun saveName() {
        if (_state.value.isSavingName) return
        val draft = _state.value.nameDraft.trim()
        val currentName = _state.value.user?.fullName.orEmpty()
        if (draft == currentName || draft.isBlank()) {
            _state.value = _state.value.copy(isEditingName = false)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingName = true)
            when (val response = userRepository.updateFullName(draft)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isSavingName = false,
                        isEditingName = false,
                        user = response.data,
                        nameDraft = response.data.fullName,
                    )
                    _effect.send(OwnProfileEffect.ShowSnackbar("Name updated"))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isSavingName = false)
                    _effect.send(OwnProfileEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun saveMobilepay() {
        if (_state.value.isSavingMobilepay) return
        val draft = _state.value.mobilepayDraft.trim()
        if (!draft.matches(Regex("""^\d{8}$"""))) {
            viewModelScope.launch {
                _effect.send(OwnProfileEffect.ShowSnackbar("MobilePay number must be exactly 8 digits"))
            }
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingMobilepay = true)
            when (val response = userRepository.updateMobilepayNumber(draft)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isSavingMobilepay = false,
                        isEditingMobilepay = false,
                        user = response.data,
                        mobilepayDraft = response.data.mobilepayNumber.orEmpty(),
                    )
                    _effect.send(OwnProfileEffect.ShowSnackbar("MobilePay number updated"))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isSavingMobilepay = false)
                    _effect.send(OwnProfileEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
