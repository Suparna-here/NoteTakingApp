package com.example.compose.notetakingapp.di

import android.content.Context
import com.example.compose.notetakingapp.data.local.NoteDAO
import com.example.compose.notetakingapp.data.local.NoteDatabase
import com.example.compose.notetakingapp.data.repository.NoteRepositoryImpl
import com.example.compose.notetakingapp.domain.repository.NoteRepository
import com.example.compose.notetakingapp.domain.use_case.AddNote
import com.example.compose.notetakingapp.domain.use_case.DeleteNote
import com.example.compose.notetakingapp.domain.use_case.GetNotes
import com.example.compose.notetakingapp.domain.use_case.NoteUseCases
import com.example.compose.notetakingapp.util.PassphraseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePassphraseManager(@ApplicationContext context: Context): PassphraseManager {
        return PassphraseManager(context)
    }

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context,
        passphraseManager: PassphraseManager
    ): NoteDatabase {
        return NoteDatabase.getDatabase(context, passphraseManager.getPassphrase())
    }

    @Provides
    @Singleton
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDAO{
        return noteDatabase.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        dao: NoteDAO,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): NoteRepository {
        return NoteRepositoryImpl(dao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            getNotes = GetNotes(repository),
            addNote = AddNote(repository),
            deleteNote = DeleteNote(repository)
        )
    }
}