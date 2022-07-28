package com.mafunzo.loop.ui.settings.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val userPrefs: AppDatasource,
    val firestoreDB: FirebaseFirestore
): ViewModel() {
    private val TAG = "SettingsViewModel"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage

    private val _submittedSuccessfully = MutableSharedFlow<Boolean>()
    val submittedSuccessfully = _submittedSuccessfully.asSharedFlow()

    private val _userDetails = MutableSharedFlow<UserResponse>()
    val userDetails = _userDetails.asSharedFlow()

    fun fetchUserData(){
        val phoneNumber = firebaseAuth.currentUser?.phoneNumber
        if(!phoneNumber.isNullOrEmpty()){
            viewModelScope.launch {
                Log.d(TAG , "fetching user: $phoneNumber")
                userPrefs.clear()
                _isLoading.value = true
                firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.toObject(UserResponse::class.java)
                        if(user != null && user.accountType?.isNotEmpty() == true) {

                            viewModelScope.launch {
                                _isLoading.value = false
                                _userDetails.emit(user)
                                //save current workspace(school id) in shared pref
                                user.schools?.let {
                                    userPrefs.saveCurrentWorkspace(it.entries.first().key.trim(), it.entries.first().value)
                                    userPrefs.saveAccountType(user.accountType)
                                    Log.d(TAG, "Save current workspace: ${it.entries.first().key.trim()}")
                                }
                            }
                        } else {
                            viewModelScope.launch {
                                _isLoading.value = false
                                Log.d(TAG, "fetchUser Error: User is null")
                            }
                        }
                    } else {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _errorMessage.emit(task.exception?.message.toString())
                            Log.d(TAG, "fetchUser Error: ${task.exception?.message}")
                        }
                    }
                }
            }
        }
    }

    fun updateUserDetails(firstName: String, secondName: String, email: String, accountType: String) {

        //update fields in firestore document
        val phoneNumber = firebaseAuth.currentUser?.phoneNumber
        if(!phoneNumber.isNullOrEmpty()){
            viewModelScope.launch {
                _isLoading.value = true
                firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).update(
                    "firstName", firstName,
                    "lastName", secondName,
                    "email", email,
                    "accountType", accountType
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _submittedSuccessfully.emit(true)
                            Log.d(TAG, "updateUserDetails Success")
                        }
                    } else {
                        viewModelScope.launch {
                            _isLoading.value = false
                            _errorMessage.emit(task.exception?.message.toString())
                            Log.d(TAG, "updateUserDetails Error: ${task.exception?.message}")
                        }
                    }
                }
            }
        }
    }
}