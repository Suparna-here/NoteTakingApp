package com.example.compose.notetakingapp.domain.use_case

import com.example.compose.notetakingapp.R
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.model.NoteException
import com.example.compose.notetakingapp.domain.repository.NoteRepository
import com.example.compose.notetakingapp.domain.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class GetNotes @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getAllNotes().catch { e ->
            throw NoteException.OperationError(
                UiText.StringResource(
                    R.string.error_loading_notes,
                    e.message ?: ""
                )
            )
        }
    }
}
