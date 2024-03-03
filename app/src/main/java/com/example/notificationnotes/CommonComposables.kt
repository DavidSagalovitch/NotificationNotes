package com.example.notificationnotes

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDismissState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ThemedButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	Button(
		onClick = onClick,
		modifier = modifier,
		enabled = enabled,
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.tertiary, // Red for buttons
			contentColor = MaterialTheme.colorScheme.onTertiary // White text/icons on tertiary color
		),
		shape = RoundedCornerShape(8.dp)
	) {
		Text(text)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedTextField(label: @Composable() (() -> Unit)?, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, singleLine: Boolean){
	TextField(
		label = label,
		value = value,
		onValueChange = onValueChange,
		keyboardOptions = KeyboardOptions.Default.copy(
		imeAction = ImeAction.Done
	),
		modifier = modifier,
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
		),
		singleLine = singleLine
	)
}

@Composable
fun ClickableTextBox(
	text: String,
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier
		.clickable(onClick = onClick)
		.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
		.background(color = MaterialTheme.colorScheme.surface)
		.padding(horizontal = 8.dp, vertical = 8.dp)
	) {
		Text(
			text = label,
			style = TextStyle(
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium
			),
			modifier = Modifier.padding(bottom = 4.dp)
		)
		Text(
			text = text,
			style = TextStyle(
				color = Color(0xFF5C5C5C),
				fontSize = 16.sp
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp)
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
	item: T,
	onDelete: (T) -> Unit,
	animationDuration: Int = 500,
	content: @Composable (T) -> Unit
) {
	var isRemoved by remember {
		mutableStateOf(false)
	}
	val state = rememberDismissState(
		confirmValueChange = { value ->
			if (value == DismissValue.DismissedToStart) {
				isRemoved = true
				true
			} else {
				false
			}
		}
	)

	LaunchedEffect(key1 = isRemoved) {
		if(isRemoved) {
			delay(animationDuration.toLong())
			onDelete(item)
		}
	}

	AnimatedVisibility(
		visible = !isRemoved,
		exit = shrinkVertically(
			animationSpec = tween(durationMillis = animationDuration),
			shrinkTowards = Alignment.Top
		) + fadeOut()
	) {
		SwipeToDismiss(
			state = state,
			background = {
				DeleteBackground(swipeDismissState = state)
			},
			dismissContent = { content(item) },
			directions = setOf(DismissDirection.EndToStart)
		)
	}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBackground(
	swipeDismissState: DismissState
) {
	val color = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart) {
		MaterialTheme.colorScheme.tertiary
	} else Color.Transparent

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(color)
			.padding(16.dp),
		contentAlignment = Alignment.CenterEnd
	) {
		Icon(
			imageVector = Icons.Default.Delete,
			contentDescription = null,
			tint = Color.White
		)
	}
}
@Composable
fun NotificationEntry(
	selectedNoteTitle: String,
	selectedNoteDescription:String,
	onBackPress: () -> Unit,
	onSubmit: (title:String, content:String) -> Unit
){
	var noteDescription by remember { mutableStateOf(selectedNoteDescription) }
	var noteTitle by remember { mutableStateOf(selectedNoteTitle) }

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

				ThemedButton(onClick ={
					onSubmit(noteTitle, noteDescription)
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