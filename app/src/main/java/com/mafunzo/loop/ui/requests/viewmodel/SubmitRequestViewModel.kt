package com.mafunzo.loop.ui.requests.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.requests.StandardRequest
import com.mafunzo.loop.data.models.responses.UserRequestResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage

    private val _submittedSuccessfully = MutableSharedFlow<Boolean>()
    val submittedSuccessfully = _submittedSuccessfully.asSharedFlow()

    private val _requestTypes = MutableLiveData<List<String>>()
    val requestTypes: LiveData<List<String>> = _requestTypes

    private val _requests = MutableLiveData<List<UserRequestResponse>>()
    val requests: LiveData<List<UserRequestResponse>> = _requests


    fun submitRequest(request: StandardRequest) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            viewModelScope.launch {
                _loading.value = true
                val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                val accountType = userPrefs.getAccountType().first()?.trim()

                if (currentWorkSpace != null && accountType != null) {
                    val submitRef = user.phoneNumber?.let {phonenumber ->
                        firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                            .document(currentWorkSpace).collection(phonenumber).document()
                    }

                    request.id = submitRef?.id.toString() //append firestore auto-generated id to request
                    if(request.id.isNotEmpty()){
                        submitRef?.set(request)?.addOnCompleteListener {
                            viewModelScope.launch {
                                _loading.value = false
                                if (it.isSuccessful) {
                                    getRequests(5)
                                    _submittedSuccessfully.emit(true)
                                }  else {
                                    viewModelScope.launch {
                                        it.exception?.message?.let { it1 -> _errorMessage.emit(it1) }
                                    }
                                }
                            }
                        }?.addOnFailureListener {
                            viewModelScope.launch {
                                _loading.value = false
                                it.message?.let { it1 -> _errorMessage.emit(it1) }
                            }
                        }
                    } else {
                        _loading.value = false
                        _errorMessage.emit("Error submitting request")
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
                            document.data?.get("types")?.let {
                                _requestTypes.value = it as List<String>
                            }
                        } else {
                           _requestTypes.value = emptyList()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _loading.value = false
                        exception.localizedMessage?.let { _errorMessage.emit(it) }
                    }
                }
        }
    }

    fun getRequests(requests: Int?) {
        val user = firebaseAuth.currentUser
        if(requests != null) {
            //fetch n number of requests-
            viewModelScope.launch {
                _loading.value = true
                val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                val accountType = userPrefs.getAccountType().first()?.trim()

                if (user != null) {

                    if (currentWorkSpace != null && accountType != null) {
                        user.phoneNumber?.let { phonenumber ->
                            firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                                .document(currentWorkSpace).collection(phonenumber).orderBy("createdAt", Query.Direction.DESCENDING).limit(requests.toLong()).get()
                                .addOnSuccessListener { document ->
                                    viewModelScope.launch {
                                        _loading.value = false
                                        if (document != null) {
                                            document.toObjects(UserRequestResponse::class.java)
                                                .let { appRequests ->
                                                    _requests.value = appRequests
                                                }
                                        } else {
                                            _requests.value = emptyList()
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    viewModelScope.launch {
                                        _loading.value = false
                                        exception.localizedMessage?.let { exception.message?.let { it1 ->
                                            _errorMessage.emit(
                                                it1
                                            )
                                        } }
                                    }
                                }
                        }
                    } else {
                        _errorMessage.emit("No work space selected")
                    }
                } else {
                    _errorMessage.emit("User not logged in")
                }
            }
        } else {
            //fetch all requests
            viewModelScope.launch {
                _loading.value = true
                val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                val accountType = userPrefs.getAccountType().first()?.trim()

                if (user != null) {
                    if (currentWorkSpace != null && accountType != null) {
                        user.phoneNumber?.let { phonenumber ->
                            firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                                .document(currentWorkSpace).collection(phonenumber).orderBy("createdAt", Query.Direction.DESCENDING).get()
                                .addOnSuccessListener { document ->
                                    viewModelScope.launch {
                                        _loading.value = false
                                        if (document != null) {
                                            document.toObjects(UserRequestResponse::class.java)
                                                .let { appRequests ->
                                                    _requests.value = appRequests
                                                }
                                        } else {
                                            _requests.value = emptyList()
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    viewModelScope.launch {
                                        _loading.value = false
                                        exception.localizedMessage?.let { exception.message?.let { it1 ->
                                            _errorMessage.emit(
                                                it1
                                            )
                                        } }
                                    }
                                }
                        }
                    } else {
                        _errorMessage.emit("No work space selected")
                    }
                } else {
                    _errorMessage.emit("User not logged in")
                }
            }
        }
    }
}