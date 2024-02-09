package com.example.notificationnotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.example.notificationnotes.proto.notificationNotesParamsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProtoDataStoreApplication : Application() {

	private val appCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	override fun onCreate() {
		super.onCreate()

		Log.d("ProtoDataStoreApplication", "onCreate")

		appCoroutineScope.launch {
			val beforeUpdate = notificationNotesParamsDataStore.data.first()
			notificationNotesParamsDataStore.updateData { currentParams ->
				val currentNoteIDs: List<Int> = currentParams.noteIDList
				val currentNoteTitles: List<String> = currentParams.noteTitleList
				val currentNoteList: List<String> = currentParams.noteList
				currentParams.toBuilder()
					.clear()
					.addAllNoteID( if (currentNoteIDs.isEmpty()) {
						listOf<Int>()
					} else {
						currentNoteIDs
					})
					.addAllNoteTitle(if (currentNoteTitles.isEmpty()){
						listOf<String>()
					}
					else{
						currentNoteList
					})
					.addAllNote( if (currentNoteList.isEmpty()) {
						listOf<String>()
					} else {
						currentNoteList
					})
					.build()
			}
			val afterUpdate = notificationNotesParamsDataStore.data.first()
			Log.d("DataStore", "Before Update: $beforeUpdate, After Update: $afterUpdate")
		}
	}


}