package com.example.notificationnotes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


const val REQUEST_NOTIFICATION_PERMISSION = 123 // You can use any unique value
val CHANNEL_ID = "my_notification_channel"

lateinit var globalappContext: Context
var noteIDList: MutableList<Int> = mutableListOf()
var noteList:MutableList<String> = mutableListOf()
var removedBySwipe = true

class MainActivity : ComponentActivity() {
	@RequiresApi(Build.VERSION_CODES.TIRAMISU)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val appContext: Context by lazy {
			this
		}
		setAppContext(appContext)
		createNotificationChannel(appContext)

		if (ActivityCompat.checkSelfPermission(
				appContext,
				Manifest.permission.POST_NOTIFICATIONS
			) != PackageManager.PERMISSION_GRANTED
		) {
			// Permission is not granted, so request the missing permission
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.POST_NOTIFICATIONS),
				REQUEST_NOTIFICATION_PERMISSION)
		}

		if (!isNotificationListenerServiceEnabled(this)) {
			openNotificationAccessSettings(this)
		}

		setContent {
			NotificationNotesTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					stateMachine(appContext)
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
	var notificationTexts by remember { mutableStateOf(viewState.note) }
	var notifcationNumber = 0
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
					notificationTexts = notificationTexts + " "
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
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(8.dp),
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
							modifier = Modifier.weight(1f)


						)

						Button(
							onClick = {
								viewModel.update(index, notificationTexts[index])
								setNotesInfo(viewState.noteID, notificationTexts)
								addNotification(context, viewState.noteID.get(index), "", notificationTexts[index])

							}
						) {
							Text("+")
						}
						Button(
							onClick = {
								removedBySwipe = false
								removeNotification(context, viewState.noteID.get(index))
								notificationTexts = notificationTexts.toMutableList().apply{
									removeAt(index)
								}
								viewModel.removeNote(index)
								setNotesInfo(viewState.noteID, notificationTexts)

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


fun addNotification(context: Context,notificationId: Int, title: String, text: String) {

	// Create a notification builder
	val builder = NotificationCompat.Builder(context, CHANNEL_ID)
		.setSmallIcon(R.drawable.ic_launcher_foreground) // Set the small icon for the notification
		.setContentTitle(title) // Set the title of the notification
		.setContentText(text) // Set the content text of the notification
		.setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the priority of the notification

	// Get the notification manager
	val notificationManager = NotificationManagerCompat.from(context)

	// Send the notification
	notificationManager.notify(notificationId, builder.build())
}

fun removeNotification(context: Context, notificationId: Int){
	// Get the notification manager
	val notificationManager = NotificationManagerCompat.from(context)
	notificationManager.cancel(notificationId)
}

private fun createNotificationChannel(context: Context) {

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		// Create the NotificationChannel.
		val name = context.getString(R.string.channel_name)
		val descriptionText = context.getString(R.string.channel_description)
		val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
		mChannel.description = descriptionText
		// Register the channel with the system. You can't change the importance
		// or other notification behaviors after this.
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(mChannel)
	}
}

fun setAppContext(context: Context){
	globalappContext = context
}

fun getAppContext(): Context{
	return globalappContext
}

fun setNotesInfo(currentID: List<Int>, currentNotes: List<String>){
	noteIDList.addAll(currentID)
	noteList.addAll(currentNotes)
}

fun getNotesIDinfo(): List<Int>{
	return noteIDList
}

fun getNoteInfo(): List<String>{
	return noteList
}


fun isNotificationListenerServiceEnabled(context: Context): Boolean {
	val contentResolver = context.contentResolver
	val enabledNotificationListeners = Settings.Secure.getString(
		contentResolver,
		"enabled_notification_listeners"
	)
	val packageName = context.packageName
	return enabledNotificationListeners?.contains(packageName) == true
}

fun openNotificationAccessSettings(context: Context) {
	val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
	context.startActivity(intent)
}