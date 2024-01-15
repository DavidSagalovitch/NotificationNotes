package com.example.notificationnotes

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi

class NotificationListener : NotificationListenerService() {

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onNotificationRemoved(sbn: StatusBarNotification?) {
		super.onNotificationRemoved(sbn)

		val notification = sbn?.notification

		val channelId = notification?.channelId

		val notificationId = sbn?.id

		if (channelId == CHANNEL_ID && notificationId != null  && removedBySwipe) {
			Log.d(TAG, "Notification Removed: ${sbn.notification}")
			val note = noteList.get(noteIDList.indexOf(notificationId))
			addNotification(globalappContext, notificationId, " ", note)
		}
		removedBySwipe = true

	}

	companion object {
		private const val TAG = "NotificationListener"
	}

}