package com.example.compose.notetakingapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDatabaseTest {

    private lateinit var db: NoteDatabase
    private lateinit var dao: NoteDAO

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.noteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertNote_returnsNoteInFlow() = runTest {
        val note = NoteEntity(
            title = "Test Title",
            content = "Test Content",
            timeStamp = 12345L,
            id = 1
        )
        dao.insert(note)

        dao.getAllNotes().test {
            val notes = awaitItem()
            assertThat(notes).contains(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteNote_removesNoteFromFlow() = runTest {
        val note = NoteEntity(
            title = "Test Title",
            content = "Test Content",
            timeStamp = 12345L,
            id = 1
        )
        dao.insert(note)
        dao.delete(note)

        dao.getAllNotes().test {
            val notes = awaitItem()
            assertThat(notes).doesNotContain(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_returnsNotesOrderedByTimestampDesc() = runTest {
        val note1 = NoteEntity(title = "T1", content = "C1", timeStamp = 100L, id = 1)
        val note2 = NoteEntity(title = "T2", content = "C2", timeStamp = 300L, id = 2)
        val note3 = NoteEntity(title = "T3", content = "C3", timeStamp = 200L, id = 3)

        dao.insert(note1)
        dao.insert(note2)
        dao.insert(note3)

        dao.getAllNotes().test {
            val notes = awaitItem()
            assertThat(notes).hasSize(3)
            assertThat(notes[0]).isEqualTo(note2) // 300L
            assertThat(notes[1]).isEqualTo(note3) // 200L
            assertThat(notes[2]).isEqualTo(note1) // 100L
            cancelAndIgnoreRemainingEvents()
        }
    }
}
