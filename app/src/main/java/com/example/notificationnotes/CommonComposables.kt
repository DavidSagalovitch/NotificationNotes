package com.example.notificationnotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
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