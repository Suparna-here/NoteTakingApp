package com.example.compose.notetakingapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.compose.notetakingapp.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val content: String,
    val timeStamp: Long
) {
    fun toNote(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            timeStamp = timeStamp
        )
    }

    companion object {
        fun fromNote(note: Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                timeStamp = note.timeStamp
            )
        }
    }
}