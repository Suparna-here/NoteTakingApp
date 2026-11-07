package com.example.compose.notetakingapp.data.repository

import app.cash.turbine.test
import com.example.compose.notetakingapp.data.local.NoteDAO
import com.example.compose.notetakingapp.data.local.NoteEntity
import com.example.compose.notetakingapp.domain.model.Note
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NoteRepositoryImplTest {

    private lateinit var repository: NoteRepositoryImpl
    private lateinit var dao: NoteDAO
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        dao = mockk()
        repository = NoteRepositoryImpl(dao, testDispatcher)
    }

    @Test
    fun `getAllNotes returns mapped notes from DAO`() = runTest(testDispatcher) {
        val entities = listOf(
            NoteEntity(1, "Title 1", "Content 1", 1000L),
            NoteEntity(2, "Title 2", "Content 2", 2000L)
        )
        val expectedNotes = entities.map { it.toNote() }
        
        every { dao.getAllNotes() } returns flowOf(entities)

        repository.getAllNotes().test {
            val actualNotes = awaitItem()
            assertThat(actualNotes).isEqualTo(expectedNotes)
            awaitComplete()
        }
    }

    @Test
    fun `getAllNotes returns empty list when DAO is empty`() = runTest(testDispatcher) {
        every { dao.getAllNotes() } returns flowOf(emptyList())

        repository.getAllNotes().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getAllNotes emits updated notes when DAO emits new values`() = runTest(testDispatcher) {
        val initialEntities = listOf(NoteEntity(1, "T1", "C1", 100L))
        val updatedEntities = listOf(
            NoteEntity(1, "T1", "C1", 100L),
            NoteEntity(2, "T2", "C2", 200L)
        )
        
        // Use MutableStateFlow to simulate Room DAO behavior (always has a current value)
        val flow = MutableStateFlow(initialEntities)
        every { dao.getAllNotes() } returns flow

        repository.getAllNotes().test {
            // Check initial emission
            assertThat(awaitItem()).isEqualTo(initialEntities.map { it.toNote() })

            // Update the flow and check next emission
            flow.value = updatedEntities
            assertThat(awaitItem()).isEqualTo(updatedEntities.map { it.toNote() })
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertNote calls DAO insert with mapped entity`() = runTest(testDispatcher) {
        val note = Note(1, "Title", "Content", 1000L)
        val entity = NoteEntity.fromNote(note)
        
        coEvery { dao.insert(any()) } returns Unit

        repository.insertNote(note)

        coVerify { dao.insert(entity) }
    }

    @Test
    fun `deleteNote calls DAO delete with mapped entity`() = runTest(testDispatcher) {
        val note = Note(1, "Title", "Content", 1000L)
        val entity = NoteEntity.fromNote(note)
        
        coEvery { dao.delete(any()) } returns Unit

        repository.deleteNote(note)

        coVerify { dao.delete(entity) }
    }
}
