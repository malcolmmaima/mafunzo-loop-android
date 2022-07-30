package com.mafunzo.loop.ui.teachers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.TeachersResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeachersViewModel @Inject constructor(
    val firestoreDB: FirebaseFirestore,
    private val userPrefs: AppDatasource
): ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _teachers = MutableLiveData<List<TeachersResponse>>()
    val teachers: LiveData<List<TeachersResponse>> = _teachers

    fun fetchTeachers() {
        viewModelScope.launch {
            val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
            val accountType = userPrefs.getAccountType().first()?.trim()
            _isLoading.value = true

            if (currentWorkSpace != null && accountType != null) {
                firestoreDB.collection(Constants.FIREBASE_TEACHERS)
                    .document(currentWorkSpace).collection(Constants.FIREBASE_TEACHERS_COLLECTION).get()
                    .addOnSuccessListener { teachersCollection ->
                        _isLoading.value = false
                        val teachers = teachersCollection.toObjects(TeachersResponse::class.java)
                        _teachers.value = teachers
                    }.addOnFailureListener {
                        _isLoading.value = false
                        _errorMessage.value = it.message
                    }
            } else {
                _isLoading.value = false
                _errorMessage.value = "No current workspace or account type found"
            }
        }
    }
}