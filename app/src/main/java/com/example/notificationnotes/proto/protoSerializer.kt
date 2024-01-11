package com.example.notificationnotes.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.notificationnotes.NotificationNotes
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream


object NotificationNotesSerializer : Serializer<NotificationNotes.notificationNote> {

	override val defaultValue: NotificationNotes.notificationNote = NotificationNotes.notificationNote.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): NotificationNotes.notificationNote = withContext(Dispatchers.IO) {
		try {
			return@withContext NotificationNotes.notificationNote.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {

			throw CorruptionException("Cannot read proto.", exception)
		}
	}

	override suspend fun writeTo(t: NotificationNotes.notificationNote, output: OutputStream) = withContext(Dispatchers.IO) { t.writeTo(output) }
}

val Context.notificationNotesParamsDataStore: DataStore<NotificationNotes.notificationNote> by dataStore(
	fileName = "notification_notes.pb",
	serializer = NotificationNotesSerializer
)