package com.example.compose.notetakingapp.domain.use_case

import app.cash.turbine.test
import com.example.compose.notetakingapp.data.repository.FakeNoteRepository
import com.example.compose.notetakingapp.domain.model.Note
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteNoteTest {

    private lateinit var deleteNote: DeleteNote
    private lateinit var fakeRepository: FakeNoteRepository

    @Before
    fun setUp() {
        fakeRepository = FakeNoteRepository()
        deleteNote = DeleteNote(fakeRepository)
    }

    @Test
    fun `Delete note, removes it from repository`() = runTest {
        val note = Note(
            title = "Title",
            content = "Content",
            timeStamp = 12345L,
            id = 1
        )
        fakeRepository.insertNote(note)

        deleteNote(note)

        fakeRepository.getAllNotes().test {
            val notes = awaitItem()
            assertThat(notes).doesNotContain(note)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
