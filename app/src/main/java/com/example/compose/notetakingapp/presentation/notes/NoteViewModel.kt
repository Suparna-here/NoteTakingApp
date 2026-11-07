package com.example.compose.notetakingapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compose.notetakingapp.R
import com.example.compose.notetakingapp.di.MainDispatcher
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.model.NoteException
import com.example.compose.notetakingapp.domain.use_case.NoteUseCases
import com.example.compose.notetakingapp.domain.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getNotes()
    }

    private fun getNotes() {
        viewModelScope.launch(mainDispatcher) {
            try {
                noteUseCases.getNotes().collect { notes ->
                    _state.update { it.copy(notes = notes) }
                }
            } catch (e: NoteException) {
                Timber.e(e, "Error loading notes from domain")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(uiText = e.uiText)
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Unexpected error loading notes")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        uiText = UiText.StringResource(
                            R.string.error_loading_notes,
                            e.message ?: ""
                        )
                    )
                )
            }
        }
    }


    fun insert(note: Note) {
        viewModelScope.launch(mainDispatcher) {
            try {
                noteUseCases.addNote(note)
                Timber.d("Note inserted successfully: %s", note.title)
                _eventFlow.emit(UiEvent.SaveNote)
            } catch (e: NoteException) {
                Timber.e(e, "Validation error while inserting note")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(uiText = e.uiText)
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Unexpected error while inserting note")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        uiText = UiText.StringResource(R.string.error_unexpected)
                    )
                )
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(mainDispatcher) {
            try {
                noteUseCases.deleteNote(note)
                Timber.d("Note deleted successfully: %s", note.title)
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        uiText = UiText.StringResource(R.string.note_deleted)
                    )
                )
            } catch (e: NoteException) {
                Timber.e(e, "Error while deleting note")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(uiText = e.uiText)
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Unexpected error while deleting note")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        uiText = UiText.StringResource(R.string.error_deleting_note)
                    )
                )
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val uiText: UiText) : UiEvent()
        object SaveNote : UiEvent()
    }
}