package com.mafunzo.loop.ui.splash.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.models.responses.SystemSettingsResponse
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val firebaseDB: FirebaseFirestore
) : ViewModel() {

    private val _isLoading = MutableSharedFlow<Boolean>()
    val isLoading = _isLoading.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _systemSettings = MutableSharedFlow<SystemSettingsResponse>()
    val systemSettings = _systemSettings.asSharedFlow()

    fun isUserLoggedIn() = auth.currentUser != null

    fun getSystemSettings() {
        viewModelScope.launch {
            _isLoading.emit(true)
            val user = auth.currentUser
            if (user != null) {
                firebaseDB.collection(Constants.FIREBASE_APP_SETTINGS).document(Constants.FIREBASE_SYSTEM_SETTINGS)
                    .get()
                    .addOnSuccessListener { systemSetts ->
                        if(systemSetts.exists()){
                            viewModelScope.launch {
                                _isLoading.emit(false)
                                systemSetts.toObject(SystemSettingsResponse::class.java)
                                    ?.let {
                                        _systemSettings.emit(it)
                                    }
                            }
                        } else {
                            viewModelScope.launch {
                                _isLoading.emit(false)
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
}