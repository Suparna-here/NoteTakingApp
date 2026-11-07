package com.example.compose.notetakingapp.domain.use_case

import javax.inject.Inject

data class NoteUseCases @Inject constructor(
    val getNotes: GetNotes,
    val addNote: AddNote,
    val deleteNote: DeleteNote
)