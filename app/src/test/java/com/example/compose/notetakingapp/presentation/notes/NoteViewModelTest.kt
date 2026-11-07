package com.example.compose.notetakingapp.presentation.notes

import app.cash.turbine.test
import com.example.compose.notetakingapp.MainDispatcherRule
import com.example.compose.notetakingapp.domain.model.Note
import com.example.compose.notetakingapp.domain.model.NoteException
import com.example.compose.notetakingapp.domain.use_case.NoteUseCases
import com.example.compose.notetakingapp.domain.util.UiText
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NoteViewModel
    private lateinit var noteUseCases: NoteUseCases

    @Before
    fun setUp() {
        noteUseCases = mockk(relaxed = true)
        // Default behavior for getNotes
        every { noteUseCases.getNotes() } returns flowOf(emptyList())
        
        viewModel = NoteViewModel(
            noteUseCases = noteUseCases,
            mainDispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Get notes updates state with notes`() = runTest {
        val notes = listOf(
            Note(title = "T1", content = "C1", timeStamp = 1L),
            Note(title = "T2", content = "C2", timeStamp = 2L)
        )
        every { noteUseCases.getNotes() } returns flowOf(notes)
        
        // Re-initialize to trigger init block with the new mock behavior
        viewModel = NoteViewModel(noteUseCases, mainDispatcherRule.testDispatcher)

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.notes).isEqualTo(notes)
        }
    }

    @Test
    fun `Insert note success, emits SaveNote event`() = runTest {
        val note = Note(title = "T", content = "C", timeStamp = 1L)
        coEvery { noteUseCases.addNote(any()) } returns Unit

        viewModel.eventFlow.test {
            viewModel.insert(note)
            assertThat(awaitItem()).isEqualTo(NoteViewModel.UiEvent.SaveNote)
        }
    }

    @Test
    fun `Insert note InvalidNote, emits Snackbar event`() = runTest {
        val note = Note(title = "", content = "C", timeStamp = 1L)
        val uiText = UiText.DynamicString("Error")
        coEvery { noteUseCases.addNote(any()) } throws NoteException.InvalidNote(uiText)

        viewModel.eventFlow.test {
            viewModel.insert(note)
            val event = awaitItem() as NoteViewModel.UiEvent.ShowSnackbar
            assertThat(event.uiText).isEqualTo(uiText)
        }
    }

    @Test
    fun `Delete note success, emits Snackbar event`() = runTest {
        val note = Note(title = "T", content = "C", timeStamp = 1L)
        coEvery { noteUseCases.deleteNote(any()) } returns Unit

        viewModel.eventFlow.test {
            viewModel.delete(note)
            val event = awaitItem() as NoteViewModel.UiEvent.ShowSnackbar
            assertThat(event.uiText).isInstanceOf(UiText.StringResource::class.java)
        }
    }
}
