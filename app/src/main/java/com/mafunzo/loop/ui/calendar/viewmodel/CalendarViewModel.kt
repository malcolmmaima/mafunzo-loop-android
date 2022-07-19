package com.mafunzo.loop.ui.calendar.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.data.models.responses.CalendarEventResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel@Inject constructor(
    val firestoreDB: FirebaseFirestore,
    private val userPrefs: AppDatasource
    ): ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _calendarEvents = MutableLiveData<List<CalendarEventResponse>>()
    val calendarEvents: LiveData<List<CalendarEventResponse>> = _calendarEvents

    fun fetchCalendar() {
        _isLoading.value = true
        viewModelScope.launch {
            val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
            val accountType = userPrefs.getAccountType().first()?.trim()
            if (currentWorkSpace != null && accountType != null) {
                firestoreDB.collection(Constants.FIREBASE_CALENDAR_EVENTS).document(currentWorkSpace).collection(accountType).get()
                    .addOnSuccessListener { result ->
                        Log.d("CalendarVM", "Successfully fetched calendar events: ${result.size()}")
                        _isLoading.value = false
                        val events = result.toObjects(CalendarEventResponse::class.java)
                        _calendarEvents.postValue(events)
                    }
                    .addOnFailureListener { exception ->
                        _isLoading.value = false
                        _errorMessage.value = exception.localizedMessage
                    }
            } else {
                _isLoading.value = false
                _errorMessage.value = "No current workspace or account type found"
            }
        }
    }
}