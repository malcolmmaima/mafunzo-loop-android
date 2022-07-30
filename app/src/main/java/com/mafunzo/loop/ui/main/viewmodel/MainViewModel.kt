package com.mafunzo.loop.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mafunzo.loop.data.models.responses.SchoolResponse
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

    private val _schools = MutableSharedFlow<List<SchoolResponse>>()
    val schools = _schools.asSharedFlow()

    fun getAccountTypes(){
        viewModelScope.launch {
            _isLoading.emit(true)
            firestoreDB.collection(Constants.FIREBASE_APP_SETTINGS)
                .document(Constants.FIREBASE_APP_ACCOUNT_TYPES).get()
                .addOnSuccessListener { document ->
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data?.get("users")}")
                            document.data?.get("users")?.let {
                                _accountTypes.emit(it as List<String>)
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

    fun getSchools(countryCode: String){
        viewModelScope.launch {
            _isLoading.emit(true)
            //empty list of schools
            val schools = mutableListOf<SchoolResponse>()
            firestoreDB.collection(Constants.FIREBASE_APP_SETTINGS)
                .document(Constants.FIREBASE_APP_SCHOOLS)
                .collection(countryCode).get().addOnSuccessListener { documents ->
                    for(document in documents){
                        viewModelScope.launch {
                            _isLoading.emit(false)
                            document.toObject<SchoolResponse>().let { school ->
                                //add school to list of schools
                                school.id = document.id
                                schools.add(school)
                                _schools.emit(schools)
                            }
                        }
                    }
                }
        }
    }
}