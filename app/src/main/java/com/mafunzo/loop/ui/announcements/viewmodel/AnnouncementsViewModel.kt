package com.mafunzo.loop.ui.announcements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel@Inject constructor(
        val firestoreDB: FirebaseFirestore,
        private val userPrefs: AppDatasource
    ) : ViewModel() {
        private val _errorMessage = MutableLiveData<String>()
        val errorMessage: LiveData<String> = _errorMessage

        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _announcements = MutableLiveData<List<AnnouncementResponse>>()
        val announcements: LiveData<List<AnnouncementResponse>> = _announcements

            fun getAnnouncements() {
                _isLoading.value = true
                viewModelScope.launch {
                    val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                    val accountType = userPrefs.getAccountType().first()?.trim()
                    if (currentWorkSpace != null && accountType != null) {
                        firestoreDB.collection(Constants.FIREBASE_SCHOOL_ANNOUNCEMENTS).document(currentWorkSpace).collection(accountType).get()
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
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = "No current workspace or account type found"
                    }
                }
            }
}
