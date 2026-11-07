package com.example.compose.notetakingapp.domain.model

import com.example.compose.notetakingapp.domain.util.UiText

sealed class NoteException(val uiText: UiText) : Exception() {
    class InvalidNote(uiText: UiText) : NoteException(uiText)
    class OperationError(uiText: UiText) : NoteException(uiText)
}
