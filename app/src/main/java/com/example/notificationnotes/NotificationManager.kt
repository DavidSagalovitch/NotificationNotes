package com.example.notificationnotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val REQUEST_NOTIFICATION_PERMISSION = 123 // You can use any unique value
val OFFLINE_CHANNEL_ID = "offline_notifications"
val ONLINE_CHANNEL_ID = "online_notificaitons"

fun addNotification(context: Context, notificationId: Int, title: String, text: String, CHANNEL_ID: String) {

	// Create a notification builder
	val builder = NotificationCompat.Builder(context, CHANNEL_ID)
		.setSmallIcon(R.drawable.ic_launcher_foreground) // Set the small icon for the notification
		.setContentTitle(title) // Set the title of the notification
		.setContentText(text) // Set the content text of the notification
		.setStyle(NotificationCompat.BigTextStyle().bigText(text))
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

fun createNotificationChannel(context: Context) {

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		// Create the NotificationChannel.
		val name1 = context.getString(R.string.channel_name)
		val descriptionText1 = context.getString(R.string.channel_description)
		val importance1 = NotificationManager.IMPORTANCE_LOW
		val mChannel1 = NotificationChannel(OFFLINE_CHANNEL_ID, name1, importance1)
		mChannel1.description = descriptionText1
		// Register the channel with the system. You can't change the importance
		// or other notification behaviors after this.
		val notificationManager1 = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager1.createNotificationChannel(mChannel1)

		val name2 = context.getString(R.string.channel_name)
		val descriptionText2 = context.getString(R.string.channel_description)
		val importance2 = NotificationManager.IMPORTANCE_LOW
		val mChannel2 = NotificationChannel(ONLINE_CHANNEL_ID, name2, importance2)
		mChannel2.description = descriptionText2
		// Register the channel with the system. You can't change the importance
		// or other notification behaviors after this.
		val notificationManager2 = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager2.createNotificationChannel(mChannel2)

	}
}

fun setNotesInfo(currentID: List<Int>, currentNotes: List<String>){
	noteIDList.addAll(currentID)
	noteList.addAll(currentNotes)
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