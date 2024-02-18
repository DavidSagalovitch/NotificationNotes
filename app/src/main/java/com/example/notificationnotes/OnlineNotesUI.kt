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
                 onBackPress:()->Unit,
                 onEntryPress:(index:Int)->Unit) {
	var notificationTexts by remember{ mutableStateOf(listOf<String>()) }
	var noteKeys = listOf<String>()

	var notesInfo by remember { mutableStateOf(listOf<OnlineNotesInfo>()) }
	var notificationIds by remember { mutableStateOf(listOf<String>()) }
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
						notesInfo = notesInfo + OnlineNotesInfo(title = " ", content = " ")
						val newNoteId = (notificationIds.get(notificationIds.lastIndex).toInt() + 2).toString()
						notificationIds = notificationIds + newNoteId
						val note = notesInfo.getOrNull(notesInfo.lastIndex)
						val noteDetails = mapOf(
							"title" to (note?.title ?: "Default Title"),
							"content" to (note?.content ?: "Default Content"),
							"noteID" to newNoteId // Assuming you're using this as a unique identifier within the user's notes collection.
						)
						addOrUpdateNoteByEmail(userText, newNoteId, noteDetails)
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
							if (notes != null && notes.isNotEmpty()) {
								// Initialize an empty list to hold OnlineNotesInfo objects

								// Process the notes
								for ((noteId, noteDetails) in notes) {
									val title = noteDetails["title"] ?: "No Title"
									val content = noteDetails["content"] ?: "No Content"
									val noteID = noteDetails["noteID"] ?: "No ID"

									// Create an OnlineNotesInfo object and add it to the list
									val noteInfo = OnlineNotesInfo(title = title, content = content)
									notificationIds = notificationIds + noteID
									notesInfo = notesInfo + noteInfo
								}

								// Now onlineNotesInfoList contains all the notes as OnlineNotesInfo objects
								// You can use this list as needed, for example, display them or process them further
							} else {
								println("No notes found or user does not exist.")
							}
						}
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

@Composable
fun NotificationEntryOnline(context:Context,index: Int, onBackPress: () -> Unit){
	var noteDescription by remember { mutableStateOf(" ") }
	var noteTitle by remember { mutableStateOf(" ") }

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

				ThemedButton(onClick ={//viewModel.update(index, noteTitle, noteDescription)
					//addNotification(context, viewState.noteID.get(index), noteTitle, noteDescription, OFFLINE_CHANNEL_ID)
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
