package com.example.compose.notetakingapp.data.repository

import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {

    private val notes = mutableListOf<Note>()
    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> {
        return notesFlow
    }

    override suspend fun insertNote(note: Note) {
        notes.add(note)
        notesFlow.value = notes.toList()
    }

    override suspend fun deleteNote(note: Note) {
        notes.remove(note)
        notesFlow.value = notes.toList()
    }
}
