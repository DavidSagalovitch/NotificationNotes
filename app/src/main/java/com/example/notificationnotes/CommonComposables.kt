package com.example.notificationnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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