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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val sharedPrefs: AppDatasource,
    val firestoreDB: FirebaseFirestore,
    val userPrefs: AppDatasource,
): ViewModel() {

    private val _schoolDetails = MutableSharedFlow<SchoolResponse>()
    val schoolDetails = _schoolDetails.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _workSpacePresent = MutableLiveData<Boolean>()
    val workSpacePresent = _workSpacePresent

    private val _workSpaceEnabled = MutableLiveData<Boolean>()
    val workSpaceEnabled = _workSpaceEnabled

    private val _schools = MutableLiveData<List<SchoolResponse>>()
    val schools = _schools

    //fetch current workspace from shared prefs
    fun getCurrentWorkspace() {
        viewModelScope.launch {
            _isLoading.value = true
            sharedPrefs.getCurrentWorkSpace().first().let {schoolWorkspace ->
                Log.d("HomeViewModel", "Current workspace: $schoolWorkspace")
                if (schoolWorkspace != null) {
                    viewModelScope.launch {
                        _workSpacePresent.value = true
                        getCurrentWorkspaceName(schoolWorkspace.trim())

                        // now check status of that workspace (if enabled or not)
                        if(sharedPrefs.getCurrentWorkSpaceEnabled().first() == true) {
                            Log.d("HomeViewModel", "Workspace is enabled")
                            _workSpaceEnabled.value = true
                        } else {
                            Log.d("HomeViewModel", "Workspace is disabled")
                            _workSpaceEnabled.value = false
                        }
                    }
                } else {
                    Log.d("HomeViewModel", "No current workspace found")
                    _workSpacePresent.value = false
                    isLoading.value = false
                }
            }
        }
    }

    //get current workspace from firestore db
    fun getCurrentWorkspaceName(schoolId: String) {
        Log.d("HomeViewModel", "Getting current details from $schoolId")

        viewModelScope.launch {
            val deviceLocale = userPrefs.getCurrentUserLocale().first()?.trim()
            if (deviceLocale != null) {
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
            } else {
                viewModelScope.launch {
                    Log.d("HomeViewModel", "No locale found")
                    _errorMessage.emit("No locale found")
                    _isLoading.value = false
                }
            }
        }
    }

    fun mySchools(schools: HashMap<String, Boolean>?) {
        //take a hashmap of school id which translate to Firestore document ids and fetch details from firestore from those specific document ids
        schools?.let {
            viewModelScope.launch {
                val deviceLocale = userPrefs.getCurrentUserLocale().first()?.trim()
                val schoolsList = ArrayList<SchoolResponse>()
                for ((schoolId, enabled) in schools) {
                    if (deviceLocale != null) {
                        firestoreDB.collection(FIREBASE_APP_SETTINGS).document(FIREBASE_APP_SCHOOLS).collection(deviceLocale).document(schoolId.trim()).get()
                            .addOnSuccessListener { document ->
                                document.toObject(SchoolResponse::class.java)
                                    ?.let { school ->
                                        schoolsList.add(school)
                                    }
                                _schools.value = schoolsList
                            }.addOnFailureListener { exception ->
                                Log.e("HomeViewModel", "get failed with ", exception)
                                viewModelScope.launch {
                                    exception.localizedMessage?.let { _errorMessage.emit(it) }
                                }
                            }
                    }
                }
            }
        }
    }
}