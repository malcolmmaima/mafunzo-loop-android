package com.mafunzo.loop.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.di.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val firestoreDB: FirebaseFirestore
) : ViewModel() {
    val TAG = "MainViewModel"

    private val _accountTypes = MutableSharedFlow<List<String>>()
    val accountTypes = _accountTypes.asSharedFlow()

    private val _isLoading = MutableSharedFlow<Boolean>()
    val isLoading = _isLoading.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun getAccountTypes(){
        viewModelScope.launch {
            _isLoading.emit(true)
            firestoreDB.collection(Constants.FIREBASE_APP_SETTINGS)
                .document(Constants.FIREBASE_APP_ACCOUNT_TYPES).get()
                .addOnSuccessListener { document ->
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            document.data?.entries?.map { it.value as String }
                                ?.let { accounttypes ->
                                    _accountTypes.emit(accounttypes)
                                }
                        } else {
                            Log.d(TAG, "No such document")
                            _accountTypes.emit(listOf())
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        exception.localizedMessage?.let { _errorMessage.emit(it) }
                    }
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }
}