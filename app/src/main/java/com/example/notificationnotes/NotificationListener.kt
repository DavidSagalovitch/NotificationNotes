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

		var note = "temp"

		if ((channelId == OFFLINE_CHANNEL_ID || channelId == ONLINE_CHANNEL_ID) && notificationId != null  && removedBySwipe) {
			Log.d(TAG, "Notification Removed: ${sbn.notification}")
			if(channelId == OFFLINE_CHANNEL_ID ) {
				note = noteList.get(noteIDList.indexOf(notificationId))
			}
			if(channelId == ONLINE_CHANNEL_ID) {
				note = onlineNoteList.get(onlineNoteIdList.indexOf(notificationId))
			}
			addNotification(globalappContext, notificationId, " ", note, channelId)
		}
		removedBySwipe = true

	}

	companion object {
		private const val TAG = "NotificationListener"
	}

}