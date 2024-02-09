package com.example.notificationnotes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.notificationnotes.ui.theme.NotificationNotesTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import com.example.notificationnotes.data.checkAndSetUserData
import com.example.notificationnotes.data.isUserSignedin
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


lateinit var globalappContext: Context
var noteIDList: MutableList<Int> = mutableListOf()
var noteList:MutableList<String> = mutableListOf()
var onlineNoteIdList: MutableList<Int> = mutableListOf()
var onlineNoteList: MutableList<String> = mutableListOf()
var removedBySwipe = true

enum class ScreenStates{
	MAIN,
	ONLINE,
	OFFLINE,
	OFFLINE_ENTRY,
}

var isUserSignedIn = mutableStateOf(false)


class MainActivity : ComponentActivity() {
	private val signInLauncher = registerForActivityResult(
		FirebaseAuthUIActivityResultContract(),
	) { res ->
		this.onSignInResult(res)
	}

	private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
		val response = result.idpResponse
		if (result.resultCode == RESULT_OK) {
			// Successfully signed in
			isUserSignedIn.value = true
			val user = FirebaseAuth.getInstance().currentUser
			user?.let {
				checkAndSetUserData(it.uid, it.email ?: "")
			}
		} else {
			isUserSignedIn.value = false
			// Sign in failed. If response is null the user canceled the
			// sign-in flow using the back button. Otherwise check
			// response.getError().getErrorCode() and handle the error.
			// ...
		}
	}

	@RequiresApi(Build.VERSION_CODES.TIRAMISU)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val appContext: Context by lazy {
			this
		}
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


		FirebaseApp.initializeApp(this);

		globalappContext = appContext

		setContent {
			NotificationNotesTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					stateMachine(appContext, launchSignInFlow= {
						if(isUserSignedin()){
							if (!isUserSignedIn.value) {
							// Define the providers inside the lambda if they are specific to the sign-in flow
							val providers = arrayListOf(
								AuthUI.IdpConfig.EmailBuilder().build(),
								AuthUI.IdpConfig.AnonymousBuilder().build(),
							)
							// Create and launch sign-in intent
							val signInIntent = AuthUI.getInstance()
								.createSignInIntentBuilder()
								.setAvailableProviders(providers)
								.build()
							signInLauncher.launch(signInIntent)
							}
						}
					},
						)
				}
			}
		}
	}
}

@Composable
fun stateMachine(context: Context, viewModel: OfflineViewModel = viewModel(), launchSignInFlow: () -> Unit) {
	var currentScreen by remember { mutableStateOf<ScreenStates>(ScreenStates.MAIN) }
	var currentEntry by remember { mutableStateOf(0) }

	when (currentScreen) {
		ScreenStates.MAIN -> loginScreen(onStateChange = { newState ->
			currentScreen = newState
			if (newState == ScreenStates.ONLINE) {
				launchSignInFlow()
			}
		})
		ScreenStates.ONLINE -> 	onlineScreen(context){
			currentScreen = ScreenStates.MAIN
		}
		ScreenStates.OFFLINE-> 	offlineScreen(context, viewModel,
			onBackPress = {
			currentScreen = ScreenStates.MAIN
		},
			onEntryPress = {
				index ->
				currentScreen = ScreenStates.OFFLINE_ENTRY
				currentEntry = index
			})
		ScreenStates.OFFLINE_ENTRY -> notificationEntryOffline(context, viewModel, currentEntry) {
			currentScreen = ScreenStates.OFFLINE
		}
	}
}

@Composable
fun loginScreen(onStateChange: (ScreenStates) -> Unit) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally


	)
	{
		Box(modifier = Modifier
			.fillMaxHeight(0.45f)) {
			Text(
				text = "Welcome To Notification Notes",
				fontSize = 30.sp,
				textAlign = TextAlign.Center

			)
		}
		Row(modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			ThemedButton(onClick = {
				onStateChange(ScreenStates.ONLINE)
				},
				modifier = Modifier
					.weight(1f)
					.padding(10.dp),
				text = "Online")
			ThemedButton(onClick = { onStateChange(ScreenStates.OFFLINE) },
				modifier = Modifier
					.weight(1f)
					.padding(10.dp),
				text = "Offline")
		}
	}
}

@Composable
fun notificationImageIcon(){

}

fun setNotesInfo(currentID: List<Int>, currentNotes: List<String>){
	noteIDList.addAll(currentID)
	noteList.addAll(currentNotes)
}


fun setOnlineNotesInfo(currentID: List<Int>, currentNotes: List<String>){
	onlineNoteIdList.addAll(currentID)
	onlineNoteList.addAll(currentNotes)
}

