package com.example.notificationnotes

import android.app.Notification
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

		val contents = notification?.extras

		val title = contents?.getString(Notification.EXTRA_TITLE)
		val text = contents?.getCharSequence(Notification.EXTRA_TEXT).toString()
		val subText = contents?.getCharSequence(Notification.EXTRA_SUB_TEXT).toString()

		if ((channelId == OFFLINE_CHANNEL_ID || channelId == ONLINE_CHANNEL_ID) && notificationId != null  && removedBySwipe) {
			Log.d(TAG, "Notification Removed: ${sbn.notification}")
			if (title != null) {
				addNotification(globalappContext, notificationId, title, text, channelId)
			}
		}
		removedBySwipe = true

	}

	companion object {
		private const val TAG = "NotificationListener"
	}

}