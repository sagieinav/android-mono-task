package dev.sagi.monotask.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel enforcing the Contract pattern (State / Event / Effect) across
 * all screen-specific ViewModels.
 *
 * Subclasses must:
 *  1. Provide [initialState]
 *  2. Implement [onEvent] to handle all UI user-initiated events
 *  3. Use [setState] to update state and [sendEffect] to emit one-shot effects
 */
abstract class BaseViewModel<S, E, Ef> : ViewModel() {

    protected abstract val initialState: S

    protected val _uiState: MutableStateFlow<S> by lazy { MutableStateFlow(initialState) }
    open val uiState: StateFlow<S> by lazy { _uiState.asStateFlow() }

    private val _effect: Channel<Ef> by lazy { Channel(Channel.BUFFERED) }
    val effect: Flow<Ef> by lazy { _effect.receiveAsFlow() }

    abstract fun onEvent(event: E)

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(effect: Ef) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
