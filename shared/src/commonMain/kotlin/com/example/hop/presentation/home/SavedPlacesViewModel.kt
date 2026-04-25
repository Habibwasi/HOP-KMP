package com.example.hop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.domain.repository.PlacesRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class SavedPlacesUiState(
    val isLoading: Boolean = false,
    val isMutating: Boolean = false,
    val places: List<SavedPlace> = emptyList(),
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface SavedPlacesEvent {
    data object Load : SavedPlacesEvent
    data class Add(
        val label: String,
        val address: String,
        val lat: Double? = null,
        val lng: Double? = null,
        val kind: SavedPlaceKind? = null,
    ) : SavedPlacesEvent
    data class Delete(val id: String) : SavedPlacesEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface SavedPlacesEffect {
    data class ShowError(val message: String) : SavedPlacesEffect
    data class PlaceSelected(val place: SavedPlace) : SavedPlacesEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class SavedPlacesViewModel(
    private val placesRepository: PlacesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedPlacesUiState())
    val state: StateFlow<SavedPlacesUiState> = _state.asStateFlow()

    private val _effect = Channel<SavedPlacesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: SavedPlacesEvent) {
        when (event) {
            is SavedPlacesEvent.Load -> load()
            is SavedPlacesEvent.Add -> add(event.label, event.address, event.lat, event.lng, event.kind)
            is SavedPlacesEvent.Delete -> delete(event.id)
        }
    }

    private fun load() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = placesRepository.list()) {
                is ApiResponse.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    places = response.data,
                )
                is ApiResponse.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = response.message,
                )
            }
        }
    }

    private fun add(
        label: String,
        address: String,
        lat: Double?,
        lng: Double?,
        kind: SavedPlaceKind?,
    ) {
        if (_state.value.isMutating) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isMutating = true, error = null)
            when (val response = placesRepository.upsert(label, address, lat, lng, kind)) {
                is ApiResponse.Success -> {
                    // Optimistically merge — replace existing entry with same id, or append.
                    val merged = _state.value.places.filterNot { it.id == response.data.id } + response.data
                    _state.value = _state.value.copy(
                        isMutating = false,
                        places = merged.sortedWith(compareBy({ it.kind.ordinal }, { it.label })),
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isMutating = false)
                    _effect.trySend(SavedPlacesEffect.ShowError(response.message))
                }
            }
        }
    }

    private fun delete(id: String) {
        if (_state.value.isMutating) return
        // Snapshot for rollback.
        val previous = _state.value.places
        _state.value = _state.value.copy(
            isMutating = true,
            places = previous.filterNot { it.id == id },
        )
        viewModelScope.launch {
            when (val response = placesRepository.delete(id)) {
                is ApiResponse.Success -> _state.value = _state.value.copy(isMutating = false)
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isMutating = false, places = previous)
                    _effect.trySend(SavedPlacesEffect.ShowError(response.message))
                }
            }
        }
    }
}
