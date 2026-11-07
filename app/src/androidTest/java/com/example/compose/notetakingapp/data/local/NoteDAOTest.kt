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
class NoteDAOTest {

    private lateinit var database: NoteDatabase
    private lateinit var dao: NoteDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.noteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getAllNotes_emitsEmptyListInitially() = runTest {
        dao.getAllNotes().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertNote_emitsCorrectList() = runTest {
        val note = NoteEntity(id = 1, title = "test", content = "content", timeStamp = 1L)
        dao.insert(note)
        
        dao.getAllNotes().test {
            val list = awaitItem()
            assertThat(list).contains(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteNote_emitsCorrectList() = runTest {
        val note = NoteEntity(id = 1, title = "test", content = "content", timeStamp = 1L)
        dao.insert(note)
        dao.delete(note)
        
        dao.getAllNotes().test {
            val list = awaitItem()
            assertThat(list).doesNotContain(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_orderedByTimestampDescending() = runTest {
        val note1 = NoteEntity(id = 1, title = "test1", content = "content1", timeStamp = 100L)
        val note2 = NoteEntity(id = 2, title = "test2", content = "content2", timeStamp = 300L)
        val note3 = NoteEntity(id = 3, title = "test3", content = "content3", timeStamp = 200L)
        
        dao.insert(note1)
        dao.insert(note2)
        dao.insert(note3)
        
        dao.getAllNotes().test {
            val list = awaitItem()
            assertThat(list).hasSize(3)
            assertThat(list[0].timeStamp).isEqualTo(300L)
            assertThat(list[1].timeStamp).isEqualTo(200L)
            assertThat(list[2].timeStamp).isEqualTo(100L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertMultipleNotes_emitsAllNotes() = runTest {
        val notes = listOf(
            NoteEntity(id = 1, title = "t1", content = "c1", timeStamp = 1L),
            NoteEntity(id = 2, title = "t2", content = "c2", timeStamp = 2L),
            NoteEntity(id = 3, title = "t3", content = "c3", timeStamp = 3L)
        )

        notes.forEach { dao.insert(it) }

        dao.getAllNotes().test {
            val list = awaitItem()
            assertThat(list).hasSize(3)
            assertThat(list).containsExactlyElementsIn(notes.reversed()) // Ordered by timestamp desc
            cancelAndIgnoreRemainingEvents()
        }
    }
}
