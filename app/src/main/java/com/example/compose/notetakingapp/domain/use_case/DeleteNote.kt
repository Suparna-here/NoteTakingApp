package com.example.compose.notetakingapp.domain.use_case

import com.example.compose.notetakingapp.R
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.model.NoteException
import com.example.compose.notetakingapp.domain.repository.NoteRepository
import com.example.compose.notetakingapp.domain.util.UiText
import javax.inject.Inject

class DeleteNote @Inject constructor(
    private val repository: NoteRepository
) {
    @Throws(NoteException::class)
    suspend operator fun invoke(note: Note) {
        try {
            repository.deleteNote(note)
        } catch (_: Exception) {
            throw NoteException.OperationError(
                UiText.StringResource(R.string.error_deleting_note)
            )
        }
    }
}
