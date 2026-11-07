package com.example.compose.notetakingapp.presentation.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.notetakingapp.R
import com.example.compose.notetakingapp.domain.model.Note
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.res.stringResource

@Composable
fun NoteScreen(modifier: Modifier = Modifier, noteViewModel: NoteViewModel = hiltViewModel()) {
    //DB updates -> Flow emits -> State Updates -> UI Recomposes
    val state by noteViewModel.state.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        noteViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is NoteViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.uiText.asString(context)
                    )
                }
                is NoteViewModel.UiEvent.SaveNote -> {
                    title = ""
                    content = ""
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(id = R.string.note_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(text = stringResource(id = R.string.note_content)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    noteViewModel.insert(
                        Note(
                            title = title,
                            content = content,
                            timeStamp = System.currentTimeMillis()
                        )
                    )
                }) {
                Text(text = stringResource(id = R.string.add_note))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Display all Notes from DB
            LazyColumn() {
                items(items = state.notes) { note ->
                    NoteItem(
                        note = note,
                        onDeleteClick = {
                            noteViewModel.delete(note)
                        }
                    )
                }
            }
        }
    }
}