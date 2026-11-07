package com.example.compose.notetakingapp.domain.use_case

import app.cash.turbine.test
import com.example.compose.notetakingapp.data.repository.FakeNoteRepository
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.model.NoteException
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddNoteTest {

    private lateinit var addNote: AddNote
    private lateinit var fakeRepository: FakeNoteRepository

    @Before
    fun setUp() {
        fakeRepository = FakeNoteRepository()
        addNote = AddNote(fakeRepository)
    }

    @Test
    fun `Add note with empty title, throws exception`() = runTest {
        val note = Note(
            title = "",
            content = "Content",
            timeStamp = 12345L
        )

        try {
            addNote(note)
        } catch (e: NoteException.InvalidNote) {
            assertThat(e.uiText).isNotNull()
            return@runTest
        }
        
        throw Exception("Expected InvalidNoteException but no exception was thrown")
    }

    @Test
    fun `Add note with empty content, throws exception`() = runTest {
        val note = Note(
            title = "Title",
            content = "",
            timeStamp = 12345L
        )

        try {
            addNote(note)
        } catch (e: NoteException.InvalidNote) {
            assertThat(e.uiText).isNotNull()
            return@runTest
        }

        throw Exception("Expected InvalidNoteException but no exception was thrown")
    }

    @Test
    fun `Add valid note, inserts into repository`() = runTest {
        val note = Note(
            title = "Title",
            content = "Content",
            timeStamp = 12345L
        )

        addNote(note)

        fakeRepository.getAllNotes().test {
            val notes = awaitItem()
            assertThat(notes).contains(note)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
