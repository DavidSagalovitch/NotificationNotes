package com.example.notificationnotes

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notificationnotes.proto.NotificationNotesSerializer
import com.example.notificationnotes.proto.notificationNotesParamsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_TIMESTAMP = 0

class MainViewModel(application: Application) : AndroidViewModel(application) {

    data class ViewState(
        val numOfNotes: Int = 1,
        val note: List<String> = listOf<String>("Enter Reminder")
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
                    numOfNotes = startupParams.numOfNotes,
                    note = startupParams.noteList.toList()
                )
            }
        }
    }

    fun update(index: Int, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
                currentParams.toBuilder()
                    .setNumOfNotes(currentParams.numOfNotes-1)
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
                    .setNumOfNotes(currentParams.numOfNotes+1)
                    .addNote("Enter Reminder")
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