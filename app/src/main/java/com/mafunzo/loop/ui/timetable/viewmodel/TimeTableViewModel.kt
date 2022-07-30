package com.mafunzo.loop.ui.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.SubjectRespone
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    val firestoreDB: FirebaseFirestore,
    private val userPrefs: AppDatasource
): ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _subjects = MutableLiveData<List<SubjectRespone>>()
    val subjects: LiveData<List<SubjectRespone>> = _subjects

    fun getTimeTable(selectedGrade: String, timeTableDay: Int) {
        //timeTableDay = 0 for Mon, 1 for Tue, 2 for Wed, 3 for Thu, 4 for Fri, 5 for Sat

        Log.d("TimeTableViewModel", "getTimeTable: $selectedGrade, $timeTableDay")
        viewModelScope.launch {
            _isLoading.value = true
            val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()

            if (currentWorkSpace != null) {
                firestoreDB.collection(Constants.FIREBASE_SUBJECTS).document(currentWorkSpace)
                    .collection(selectedGrade.lowercase()).whereEqualTo("dayOfWeek", timeTableDay)
                    .get().addOnSuccessListener { subs ->
                        Log.d("TimeTableViewModel", "Value: ${subs.size()}.")
                        _isLoading.value = false
                        _subjects.value = subs.toObjects(SubjectRespone::class.java)
                    }.addOnFailureListener {
                        _isLoading.value = false
                        _errorMessage.value = it.message
                    }
            }
        }
    }
}