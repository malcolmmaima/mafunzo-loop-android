package com.mafunzo.loop.ui.splash.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.SystemSettingsResponse
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val firebaseDB: FirebaseFirestore,
    val firestoreDB: FirebaseFirestore,
    val userPrefs: AppDatasource
) : ViewModel() {

    private val TAG = "SplashViewModel"
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _systemSettings = MutableLiveData<SystemSettingsResponse>()
    val systemSettings: LiveData<SystemSettingsResponse> = _systemSettings

    val userPhoneNumber = auth.currentUser?.phoneNumber

    private val _userExists = MutableLiveData<Boolean>()
    val userExists: LiveData<Boolean> = _userExists

    private val _userEnabled = MutableSharedFlow<Boolean>()
    val userEnabled = _userEnabled.asSharedFlow()

    private val _userDetails = MutableSharedFlow<UserResponse>()
    val userDetails = _userDetails.asSharedFlow()

    private val _loggedOut = MutableSharedFlow<Boolean>()
    val loggedOut = _loggedOut.asSharedFlow()

    fun isUserLoggedIn() = auth.currentUser != null

    fun getSystemSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = auth.currentUser
            if (user != null) {
                firebaseDB.collection(Constants.FIREBASE_APP_SETTINGS).document(Constants.FIREBASE_SYSTEM_SETTINGS)
                    .get()
                    .addOnSuccessListener { systemSetts ->
                        if(systemSetts.exists()){
                            viewModelScope.launch {
                                _isLoading.value = false
                                systemSetts.toObject(SystemSettingsResponse::class.java)
                                    ?.let {
                                        _systemSettings.value = it
                                    }
                            }
                        } else {
                            viewModelScope.launch {
                                _isLoading.value = false
                                _errorMessage.emit("System settings not found")
                            }
                        }
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _errorMessage.emit(it.message ?: "")
                        }
                    }
            }
        }
    }

    fun fetchUser(phoneNumber: String){
        Log.d(TAG , "fetchUser")
        if(phoneNumber.isNotEmpty()){
            viewModelScope.launch {
                Log.d(TAG , "fetching user: $phoneNumber")
                _isLoading.value = true
            }
            firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.toObject(UserResponse::class.java)
                    Log.d(TAG , "user: $user")
                    if(user != null && user.accountType?.isNotEmpty() == true) {

                        viewModelScope.launch {
                            user.enabled?.let { _userEnabled.emit(it) }
                            _isLoading.value = false
                            _userExists.value = true
                            _userDetails.emit(user)
                            //save current workspace(school id) in shared pref
                            user.schools?.let {
                                viewModelScope.launch {
                                    val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                                    if(currentWorkSpace == null) {
                                        userPrefs.saveCurrentWorkspace(it.entries.first().key.trim(), it.entries.first().value)
                                    }
                                    userPrefs.saveAccountType(user.accountType)
                                }
                            }
                        }
                    } else {
                        viewModelScope.launch {
                            _userExists.value = false
                            _isLoading.value = false
                            Log.d(TAG, "fetchUser Error: User is null")
                        }
                    }
                } else {
                    viewModelScope.launch {
                        _isLoading.value = false
                        _userExists.value = false
                        _errorMessage.emit(task.exception?.message.toString())
                        Log.d(TAG, "fetchUser Error: ${task.exception?.message}")
                    }
                }
            }
        }
    }
}