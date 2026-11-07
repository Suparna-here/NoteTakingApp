package com.example.compose.notetakingapp.presentation.notes

import com.example.compose.notetakingapp.domain.model.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)