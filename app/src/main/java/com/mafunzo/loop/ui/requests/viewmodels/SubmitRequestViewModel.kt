package com.mafunzo.loop.ui.requests.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.requests.StandardRequest
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitRequestViewModel @Inject constructor(
    val firestoreDB: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,
    val userPrefs: AppDatasource
): ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _submittedSuccessfully = MutableLiveData<Boolean>()
    val submittedSuccessfully: LiveData<Boolean> = _submittedSuccessfully

    private val _requestTypes = MutableLiveData<List<String>>()
    val requestTypes: LiveData<List<String>> = _requestTypes

    fun submitRequest(request: StandardRequest) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            viewModelScope.launch {
                _loading.value = true
                val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                val accountType = userPrefs.getAccountType().first()?.trim()

                if (currentWorkSpace != null && accountType != null) {
                    val requestRef = user.phoneNumber?.let {phonenumber ->
                        firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                            .document(currentWorkSpace).collection(phonenumber).document().set(request)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    _submittedSuccessfully.value = true
                                }  else {
                                    _errorMessage.value = it.exception?.message
                                }
                            }
                            .addOnFailureListener {
                                _errorMessage.value = it.message
                            }
                    }
                }
            }
        }
    }

    fun getRequestTypes() {
        viewModelScope.launch {
            _loading.value = true
            firestoreDB.collection(Constants.FIREBASE_APP_SETTINGS)
                .document(Constants.FIREBASE_APP_REQUEST_TYPES).get()
                .addOnSuccessListener { document ->
                    viewModelScope.launch {
                        _loading.value = false
                        if (document != null) {
                            document.data?.entries?.map { it.value as String }
                                ?.let { appRequestTypes ->
                                    _requestTypes.value = appRequestTypes
                                }
                        } else {
                           _requestTypes.value = emptyList()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _loading.value = false
                        exception.localizedMessage?.let { _errorMessage.value = it }
                    }
                }
        }
    }
}