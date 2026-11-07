package com.example.compose.notetakingapp.domain.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val timeStamp: Long
)