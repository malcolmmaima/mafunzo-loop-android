package com.mafunzo.loop.ui.announcements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel@Inject constructor(val firestoreDB: FirebaseFirestore) : ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _announcements = MutableLiveData<List<AnnouncementResponse>>()
    val announcements: LiveData<List<AnnouncementResponse>> = _announcements

        fun getAnnouncements(schoolId: String, accountType: String) {
            _isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                firestoreDB.collection(Constants.FIREBASE_SCHOOL_ANNOUNCEMENTS).document(schoolId).collection(accountType).get()
                    .addOnSuccessListener { result ->
                        Log.d("AnnouncementVM", "Successfully fetched announcements: ${result.size()}")
                        _isLoading.value = false
                        val announcements = result.toObjects(AnnouncementResponse::class.java)
                        _announcements.postValue(announcements)
                    }
                    .addOnFailureListener { exception ->
                        _isLoading.value = false
                        _errorMessage.value = exception.localizedMessage
                    }
            }
        }
}
