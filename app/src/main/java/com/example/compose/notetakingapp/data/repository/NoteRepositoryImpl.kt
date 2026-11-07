package com.example.compose.notetakingapp.data.repository

import com.example.compose.notetakingapp.data.local.NoteDAO
import com.example.compose.notetakingapp.data.local.NoteEntity
import com.example.compose.notetakingapp.di.IoDispatcher
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDAO,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        Timber.d("Repository: Fetching all notes from DAO")
        return dao.getAllNotes()
            .map { entities ->
                entities.map { it.toNote() }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun insertNote(note: Note) = withContext(ioDispatcher) {
        Timber.d("Repository: Inserting note: %s", note.title)
        dao.insert(NoteEntity.fromNote(note))
    }

    override suspend fun deleteNote(note: Note) = withContext(ioDispatcher) {
        Timber.d("Repository: Deleting note: %s", note.title)
        dao.delete(NoteEntity.fromNote(note))
    }
}