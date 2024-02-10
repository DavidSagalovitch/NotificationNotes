package com.example.notificationnotes

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID

data class OfflineNotesInfo(val key: String = UUID.randomUUID().toString(), val title:String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun offlineScreen(context: Context, viewModel: OfflineViewModel,
                  onBackPress:()->Unit,
                  onEntryPress:(index:Int)->Unit) {
	val viewState: OfflineViewModel.ViewState by viewModel.viewState.collectAsStateWithLifecycle()

	var notesInfo by remember { mutableStateOf(listOf<OfflineNotesInfo>()) }

	LaunchedEffect(key1 = "combineNoteInfo") {
		val combinedNotes = viewState.noteTitle.zip(viewState.note) { title, content ->
			OfflineNotesInfo(title = title, content = content)
		}
		notesInfo = combinedNotes
	}

	Scaffold(
		bottomBar = {
			BottomAppBar(
				containerColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			) {
				ThemedButton(
					onClick = { onBackPress() },
					modifier = Modifier.padding(8.dp),
					text = "<")

				Spacer(modifier = Modifier.weight(1f))

				ThemedButton(onClick ={viewModel.addEntry()
					notesInfo = notesInfo + OfflineNotesInfo(title = " ", content = " ")
				},
					modifier = Modifier.padding(8.dp),
					text = "Add New Notification")
			}
		},
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,

			)
		{
			Text(
				text = "Notification Notes",
				fontSize = 30.sp,
				modifier = Modifier.padding(vertical = 16.dp),
				textAlign = TextAlign.Center
			)
			LazyColumn() {
				items(items = notesInfo, key = {it.key}) { note ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.wrapContentHeight(), // Ensure the Row doesn't take more vertical space than needed
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically,

						){
						SwipeToDeleteContainer(
							item = note,
							onDelete ={
							removedBySwipe = false
							removeNotification(context, viewState.noteID.get(notesInfo.indexOf(note)))
								viewModel.removeNote(notesInfo.indexOf(note))
								notesInfo = notesInfo.filterNot{it.key == note.key}

						} ) {
							ClickableTextBox(
								text = note.content,
								label = note.title,
								onClick = { onEntryPress(notesInfo.indexOf(note))},
								modifier = Modifier
									.weight(1f)
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun notificationEntryOffline(context:Context, viewModel: OfflineViewModel,index: Int, onBackPress: () -> Unit){
	val viewState: OfflineViewModel.ViewState by viewModel.viewState.collectAsStateWithLifecycle()
	var noteDescription by remember { mutableStateOf(viewState.note.get(index)) }
	var noteTitle by remember { mutableStateOf(viewState.noteTitle.get(index)) }

	Scaffold(
		bottomBar = {
			BottomAppBar(
				containerColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			) {
				ThemedButton(
					onClick = { onBackPress() },
					modifier = Modifier.padding(8.dp),
					text = "<")

				Spacer(modifier = Modifier.weight(1f))

				ThemedButton(onClick ={viewModel.update(index, noteTitle, noteDescription)
					addNotification(context, viewState.noteID.get(index), noteTitle, noteDescription, OFFLINE_CHANNEL_ID)
					onBackPress()
				},
					modifier = Modifier.padding(8.dp),
					text = "Update Notification")
			}
		},
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
		)
		{
			Text(
				text = "Notification Notes",
				fontSize = 30.sp,
				modifier = Modifier.padding(vertical = 16.dp),
				textAlign = TextAlign.Center
			)
			TextField(
				label = { Text("Title") },
				value = noteTitle,
				onValueChange = { newText ->
					noteTitle = newText
				},
				keyboardOptions = KeyboardOptions.Default.copy(
					imeAction = ImeAction.Done
				),
				modifier = Modifier
					.padding(start = 8.dp, end = 8.dp)
					.fillMaxWidth()
					.heightIn(max = 56.dp),
				singleLine = true,
				textStyle = TextStyle(
					color = Color(0xFF5C5C5C),
					fontSize = 16.sp // Set the font size as needed
				),
				colors = TextFieldDefaults.textFieldColors(
					containerColor = MaterialTheme.colorScheme.surface, // Darkish grey background for the TextField
					cursorColor = MaterialTheme.colorScheme.primary,
					focusedLabelColor = MaterialTheme.colorScheme.tertiary,
					focusedIndicatorColor = MaterialTheme.colorScheme.tertiary, // Use tertiary color for the bottom indicator line when focused
					unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Use a lighter color for the bottom indicator line when unfocused
				)
			)
			TextField(
				label = { Text("Content") },
				value = noteDescription,
				onValueChange = { newText ->
					noteDescription = newText
				},
				keyboardOptions = KeyboardOptions.Default.copy(
					imeAction = ImeAction.Done
				),
				modifier = Modifier
					.weight(1f)
					.padding(8.dp)
					.fillMaxSize(),
				textStyle = TextStyle(
					color = Color(0xFF5C5C5C),
					fontSize = 16.sp // Set the font size as needed
				),
				colors = TextFieldDefaults.textFieldColors(
					containerColor = MaterialTheme.colorScheme.surface, // Darkish grey background for the TextField
					cursorColor = MaterialTheme.colorScheme.primary,
					focusedLabelColor = MaterialTheme.colorScheme.tertiary,
					focusedIndicatorColor = MaterialTheme.colorScheme.tertiary, // Use tertiary color for the bottom indicator line when focused
					unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Use a lighter color for the bottom indicator line when unfocused
				)
			)
		}
	}
}