package com.example.notificationnotes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notificationnotes.ui.theme.NotificationNotesTheme
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notificationnotes.notificationNote
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel




const val REQUEST_NOTIFICATION_PERMISSION = 123 // You can use any unique value

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		createNotificationChannel(this)
		if (ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.POST_NOTIFICATIONS
			) != PackageManager.PERMISSION_GRANTED
		) {
			// Permission is not granted, so request the missing permission
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.POST_NOTIFICATIONS),
				REQUEST_NOTIFICATION_PERMISSION)
		}
		setContent {
			NotificationNotesTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					stateMachine(this)
				}
			}
		}
	}
}

@Composable
fun stateMachine(context: Context, viewModel: MainViewModel = viewModel()) {
	var currentScreen by remember { mutableStateOf<String>("login") }

	if (currentScreen == "login") {
		loginScreen {
			currentScreen = "main"
		}
	} else if (currentScreen == "main") {
		mainScreen(context, viewModel){
			currentScreen = "login"
		}
	}
}

@Composable
fun loginScreen(onLoginSuccess: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally

	)
	{
		Text(
			text = "Welcome To Notification Notes",
			fontSize = 30.sp,
			textAlign = TextAlign.Center
		)
		Row {
			Button(onClick = {  }) {
				Text("?????")
			}
			Button(onClick = { onLoginSuccess() }) {
				Text("Enter")
			}
		}
	}
}

@Composable
fun mainScreen(context: Context, viewModel: MainViewModel,
               onBackPress:()->Unit) {
	val viewState: MainViewModel.ViewState by viewModel.viewState.collectAsStateWithLifecycle()
	var text by remember { mutableStateOf("Enter New Notification") }
	var notificationTexts by remember { mutableStateOf(viewState.note) }
	Scaffold(
		bottomBar = {
			BottomAppBar(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.primary,
			) {
				Button(onClick = { onBackPress() }) {
					Text("<")
				}
				Button(onClick ={viewModel.addEntry()
					notificationTexts = notificationTexts + "Enter New Notification"
				}){
					Text("Add New Notification")

				}
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
			LazyColumn() {
				items(notificationTexts.size) { index ->
					Row(){
						// Initial text box
						// Display all notification text fields
						var editText by rememberSaveable {mutableStateOf(notificationTexts[index]) }
						text = editText
						// Editable text box
						TextField(
							value = editText,
							onValueChange = {newText ->
								editText = newText
								notificationTexts = notificationTexts.toMutableList().apply {
									this[index] = newText
								} },
							label = { Text("Notification") }
						)

						Button(
							onClick = {
								notificationTexts = notificationTexts.toMutableList().apply {
									this[index] = editText
								}
								viewModel.update(index, editText)
								sendNotification(context, viewState.noteID.get(index), "", editText)
							},
						) {
							Text("+")
						}
						Button(
							onClick = {
								if(notificationTexts.size > 1) {
									notificationTexts = notificationTexts.toMutableList().apply{
										removeAt(index)
									}
									viewModel.removeNote(index)
								}

							},

							) {
							Text("-")
						}
					}
				}
			}
		}
	}
}


fun sendNotification(context: Context,notificationId: Int, title: String, text: String) {
	val channelId = "my_notification_channel" // Use the same channel ID you used when creating the channel

	// Create a notification builder
	val builder = NotificationCompat.Builder(context, channelId)
		.setSmallIcon(R.drawable.ic_launcher_foreground) // Set the small icon for the notification
		.setContentTitle(title) // Set the title of the notification
		.setContentText(text) // Set the content text of the notification
		.setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the priority of the notification

	// Get the notification manager
	val notificationManager = NotificationManagerCompat.from(context)

	// Send the notification
	notificationManager.notify(notificationId, builder.build())
}

private fun createNotificationChannel(context: Context) {

    val CHANNEL_ID = "my_notification_channel"

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		// Create the NotificationChannel.
		val name = context.getString(R.string.channel_name)
		val descriptionText = context.getString(R.string.channel_description)
		val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
		mChannel.description = descriptionText
		// Register the channel with the system. You can't change the importance
		// or other notification behaviors after this.
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(mChannel)
	}
}
