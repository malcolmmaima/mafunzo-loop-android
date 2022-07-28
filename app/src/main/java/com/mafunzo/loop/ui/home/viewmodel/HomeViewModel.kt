package com.mafunzo.loop.ui.home.viewmodel

import android.content.res.Resources
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.di.Constants.FIREBASE_APP_SCHOOLS
import com.mafunzo.loop.di.Constants.FIREBASE_APP_SETTINGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val sharedPrefs: AppDatasource,
    val firestoreDB: FirebaseFirestore,
): ViewModel() {

    private val _schoolDetails = MutableSharedFlow<SchoolResponse>()
    val schoolDetails = _schoolDetails

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _workSpacePresent = MutableSharedFlow<Boolean>()
    val workSpacePresent = _workSpacePresent

    private val _workSpaceEnabled = MutableSharedFlow<Boolean>()
    val workSpaceEnabled = _workSpaceEnabled

    //fetch current workspace from shared prefs
    fun getCurrentWorkspace() {
        viewModelScope.launch {
            _isLoading.value = true
            sharedPrefs.getCurrentWorkSpace().first().let {schoolWorkspace ->
                Log.d("HomeViewModel", "Current workspace: $schoolWorkspace")
                if (schoolWorkspace != null) {
                    viewModelScope.launch {
                        _workSpacePresent.emit(true)
                        getCurrentWorkspaceName(schoolWorkspace.trim())

                        // now check status of that workspace (if enabled or not)
                        if(sharedPrefs.getCurrentWorkSpaceEnabled().first() == true) {
                            Log.d("HomeViewModel", "Workspace is enabled")
                            _workSpaceEnabled.emit(true)
                        } else {
                            Log.d("HomeViewModel", "Workspace is disabled")
                            _workSpaceEnabled.emit(false)
                        }
                    }
                } else {
                    Log.d("HomeViewModel", "No current workspace found")
                    viewModelScope.launch {
                        _workSpacePresent.emit(false)
                        isLoading.value = false
                    }
                }
            }
        }
    }

    //get current workspace from firestore db
    fun getCurrentWorkspaceName(schoolId: String) {
        Log.d("HomeViewModel", "Getting current details from $schoolId")
        //get device current local e.g. "KE" for Kenya
        val deviceLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].country
        viewModelScope.launch {
            firestoreDB.collection(FIREBASE_APP_SETTINGS).document(FIREBASE_APP_SCHOOLS).collection(deviceLocale).document(schoolId.trim()).get()
                .addOnSuccessListener { document ->
                    isLoading.value = false
                    if (document.exists()) {
                        viewModelScope.launch {
                            Log.d("HomeViewModel", "DocumentSnapshot data: ${document.data}")
                            document.toObject(SchoolResponse::class.java)
                                ?.let { _schoolDetails.emit(it) }
                        }
                    } else {
                        viewModelScope.launch {
                            Log.d("HomeViewModel", "No such document")
                            _errorMessage.emit("No workspace found")
                        }
                    }
                }.addOnFailureListener { exception ->
                    viewModelScope.launch {
                        Log.d("HomeViewModel", "get failed with ", exception)
                        exception.localizedMessage?.let { _errorMessage.emit(it) }
                    }
                }
        }
    }
}