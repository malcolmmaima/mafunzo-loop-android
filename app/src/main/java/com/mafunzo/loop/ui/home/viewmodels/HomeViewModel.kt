package com.mafunzo.loop.ui.home.viewmodels

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val sharedPrefs: AppDatasource,
    val firestoreDB: FirebaseFirestore,
): ViewModel() {

    private val _schoolDetails = MutableLiveData<SchoolResponse>()
    val schoolDetails = _schoolDetails

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    //fetch current workspace from shared prefs
    fun getCurrentWorkspace() {
        viewModelScope.launch {
            _isLoading.value = true
            sharedPrefs.getCurrentWorkSpace().first().let {schoolWorkspace ->
                Log.d("HomeViewModel", "Current workspace: $schoolWorkspace")
                if (schoolWorkspace != null) {
                    getCurrentWorkspaceName(schoolWorkspace.trim())
                } else {
                    Log.d("HomeViewModel", "No current workspace found")
                    isLoading.value = false
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
                        Log.d("HomeViewModel", "DocumentSnapshot data: ${document.data}")
                        _schoolDetails.value = document.toObject(SchoolResponse::class.java)
                    } else {
                        Log.d("HomeViewModel", "No such document")
                        _errorMessage.value = "No workspace found"
                    }
                }.addOnFailureListener { exception ->
                    Log.d("HomeViewModel", "get failed with ", exception)
                    _errorMessage.value = exception.localizedMessage
                }
        }
    }
}