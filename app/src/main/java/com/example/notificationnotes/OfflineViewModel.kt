package com.example.notificationnotes

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notificationnotes.proto.notificationNotesParamsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class OfflineViewModel(application: Application) : AndroidViewModel(application) {

    data class ViewState(
        val noteID: List<Int> = listOf<Int>(),
        val note: List<String> = listOf<String>()
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    private var notificationNotesCollectorJob: Job? = null

    private val notificationNotesParamsDataStore: DataStore<NotificationNotes.notificationNote>
        get() = getApplication<Application>().applicationContext.notificationNotesParamsDataStore

    init {
        // Use viewModelScope to launch the coroutine
        viewModelScope.launch(Dispatchers.IO) {
            notificationNotesParamsDataStore.data.collectLatest { startupParams: NotificationNotes.notificationNote ->
                // Update the view state when data changes
                _viewState.value = _viewState.value.copy(
                    noteID= startupParams.noteIDList.toList(),
                    note = startupParams.noteList.toList()
                )
            }
        }
    }

    fun update(index: Int, note: String) {
        viewModelScope.launch(Dispatchers.Unconfined) {
            notificationNotesParamsDataStore.updateData { currentParams ->
                currentParams.toBuilder()
                    .setNote(index, note)
                    .build()
            }
        }
    }

    fun removeNote(index: Int){

        viewModelScope.launch(Dispatchers.IO) {

            notificationNotesParamsDataStore.updateData { currentParams ->
                var updatedNoteList: List<String> = currentParams.noteList
                updatedNoteList = updatedNoteList.toMutableList().apply{
                    removeAt(index)
                }
                var updatedNoteID: List<Int> = currentParams.noteIDList
                updatedNoteID = updatedNoteID.toMutableList().apply {
                    removeAt(index)
                }
                currentParams.toBuilder()
                    .clearNoteID()
                    .addAllNoteID(updatedNoteID)
                    .clearNote()
                    .addAllNote(updatedNoteList)
                    .build()
            }
        }
    }

    fun addEntry(){
        viewModelScope.launch(Dispatchers.IO) {
            notificationNotesParamsDataStore.updateData { currentParams ->
                currentParams.toBuilder()
                    .addNoteID(if (currentParams.noteIDList.isEmpty()) 1
                    else currentParams.noteIDList.get(currentParams.noteIDList.lastIndex)+2)
                    .addNote(" ")
                    .build()
            }

            Log.d("MyApp", "ViewState: $viewState")

        }
    }

    fun resetData() {
        viewModelScope.launch(Dispatchers.IO) {
	        notificationNotesParamsDataStore.updateData { NotificationNotes.notificationNote.getDefaultInstance() }
        }
    }

    override fun onCleared() {
        notificationNotesCollectorJob?.cancel()
        super.onCleared()
    }


}