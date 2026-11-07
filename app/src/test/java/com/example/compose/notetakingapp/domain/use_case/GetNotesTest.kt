package com.example.compose.notetakingapp.domain.use_case

import app.cash.turbine.test
import com.example.compose.notetakingapp.data.repository.FakeNoteRepository
import com.example.compose.notetakingapp.domain.model.Note
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetNotesTest {

    private lateinit var getNotes: GetNotes
    private lateinit var fakeRepository: FakeNoteRepository

    @Before
    fun setUp() {
        fakeRepository = FakeNoteRepository()
        getNotes = GetNotes(fakeRepository)
    }

    @Test
    fun `Get notes, returns notes from repository`() = runTest {
        val notesToInsert = mutableListOf<Note>()
        ('a'..'z').forEachIndexed { index, c ->
            notesToInsert.add(
                Note(
                    title = c.toString(),
                    content = c.toString(),
                    timeStamp = index.toLong(),
                    id = index
                )
            )
        }
        
        notesToInsert.forEach { fakeRepository.insertNote(it) }

        getNotes().test {
            val notes = awaitItem()
            assertThat(notes).hasSize(notesToInsert.size)
            assertThat(notes).containsExactlyElementsIn(notesToInsert)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
