package com.example.notificationnotes

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.example.notificationnotes.data.addOrUpdateNoteByEmail
import com.example.notificationnotes.data.doesUserExist
import com.example.notificationnotes.data.extractIntegerFromNoteID
import com.example.notificationnotes.data.findNotesByEmail
import com.example.notificationnotes.data.removeNoteByEmail
import com.example.notificationnotes.data.signOut
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.util.UUID


data class OnlineNotesInfo(val key: String = UUID.randomUUID().toString(), val title:String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun onlineScreen(context: Context,
                 onBackPress:()->Unit) {
	var notificationTexts by remember{ mutableStateOf(listOf<String>()) }
	var notificationIds = listOf<String>()
	var notifcationNumber = 0
	var userText by remember { mutableStateOf("Enter User Name") }

	val database = Firebase.database
	lateinit var usersNotes: DatabaseReference
	var userSet = false

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

				ThemedButton(onClick = { signOut()
					onBackPress()},
					modifier = Modifier.padding(8.dp),
					text = "Sign Out")

				Spacer(modifier = Modifier.weight(1f))

				ThemedButton(onClick ={
					if(userSet) {
						notificationTexts = notificationTexts + " "
						val newNoteId = "noteId${extractIntegerFromNoteID(notificationIds.get(notificationIds.lastIndex)) +2}"
						notificationIds = notificationIds + newNoteId
						addOrUpdateNoteByEmail(userText, newNoteId, " ")
					}
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
			horizontalAlignment = Alignment.CenterHorizontally
		)
		{
			Text(
				text = "Notification Notes",
				fontSize = 30.sp,
				modifier = Modifier.padding(vertical = 16.dp),
				textAlign = TextAlign.Center
			)
			Row(modifier = Modifier
				.fillMaxWidth() ,
				horizontalArrangement = Arrangement.SpaceBetween){
				TextField(
					value = userText,
					onValueChange = { newText ->
						userText = newText
					},
					label = {
						Text("UserName")
					},
					keyboardOptions = KeyboardOptions.Default.copy(
						imeAction = ImeAction.Done
					),
					modifier = Modifier
						.weight(1f)
						.padding(start = 8.dp),
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
				ThemedButton(onClick = { doesUserExist(userText){exists ->
					if (exists){
						userSet = true
						findNotesByEmail(userText) { notes ->
							if (notes != null) {
								// Process the notes
								notificationIds = notes.keys.toList()
								notificationTexts = notes.values.toList()

							} else {
								println("No notes found or user does not exist.")
							}
						}
						Log.d("UserCheck", "User exists.")
					}
					else{
						userSet = false
						notificationTexts = listOf<String>()
						Log.d("UserCheck", "User does not exist.")
					}}
				},
					modifier = Modifier.padding(start = 4.dp, end = 8.dp),
					text = "Enter"
				)
			}
			LazyColumn() {
				items(notificationTexts.size) { index ->
					Row(
						modifier = Modifier
							.fillMaxWidth() ,
						horizontalArrangement = Arrangement.SpaceBetween){
						TextField(
							value = notificationTexts[index],
							onValueChange = {newText ->
								notificationTexts = notificationTexts.toMutableList().apply {
									this[index] = newText
								} },
							label = {
								notifcationNumber = index+1
								Text("Notification $notifcationNumber") },
							keyboardOptions = KeyboardOptions.Default.copy(
								imeAction = ImeAction.Done
							),
							modifier = Modifier
								.weight(1f)
								.padding(start = 8.dp),
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

						ThemedButton(
							onClick = {
								addOrUpdateNoteByEmail(userText, notificationIds[index], notificationTexts[index])
								addNotification(context, extractIntegerFromNoteID(notificationIds[index]), "", notificationTexts[index], ONLINE_CHANNEL_ID)
								val integersofIDs = notificationIds.map { id ->
									extractIntegerFromNoteID(id)
								}
								setOnlineNotesInfo(integersofIDs, notificationTexts)
							},
							text = "+",
							modifier = Modifier.padding(start = 8.dp, end = 4.dp)
						)
						ThemedButton(
							onClick = {
								removedBySwipe = false
								removeNotification(context, extractIntegerFromNoteID(notificationIds[index]))
								removeNoteByEmail(userText, notificationTexts[index])
								notificationTexts = notificationTexts.toMutableList().apply{
									removeAt(index)
								}
								notificationIds = notificationIds.toMutableList().apply {
									removeAt(index)
								}
								val integersofIDs = notificationIds.map { id ->
									extractIntegerFromNoteID(id)
								}
								setOnlineNotesInfo(integersofIDs, notificationTexts)

							},
							text = "-",
							modifier = Modifier.padding(start = 4.dp, end = 8.dp)

						)
					}
				}
			}
		}
	}
}

@Composable
fun NotificationEntryOnline(context:Context, viewModel: OfflineViewModel,index: Int, onBackPress: () -> Unit){
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
			ThemedTextField(
				label = { Text("Title") },
				value = noteTitle,
				onValueChange = { newText ->
					noteTitle = newText
				},
				modifier = Modifier
					.padding(start = 8.dp, end = 8.dp)
					.fillMaxWidth()
					.heightIn(max = 56.dp),
				singleLine = true,
			)
			ThemedTextField(
				label = { Text("Content") },
				value = noteDescription,
				onValueChange = { newText ->
					noteDescription = newText
				},
				modifier = Modifier
					.weight(1f)
					.padding(8.dp)
					.fillMaxSize(),
				singleLine = false
			)
		}
	}
}
