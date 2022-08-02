package com.mafunzo.loop.ui.schools.viewmodel

import android.content.res.Resources
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchoolsViewModel@Inject constructor(
        val firestoreDB: FirebaseFirestore,
        private val userPrefs: AppDatasource,
        private val firebaseAuth: FirebaseAuth
    ) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _foundSchools = MutableLiveData<List<SchoolResponse>>()
    val foundSchools: LiveData<List<SchoolResponse>> = _foundSchools

    private val _schoolAdded = MutableLiveData<Boolean>()
    val schoolAdded: LiveData<Boolean> = _schoolAdded

    fun searchSchools(schoolName: String, resultLimit: Long) {
                _isLoading.value = true
                viewModelScope.launch {
                    val deviceLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].country
                    firestoreDB.collection(Constants.FIREBASE_APP_SETTINGS).document(Constants.FIREBASE_APP_SCHOOLS)
                        .collection(deviceLocale).limit(resultLimit).get().addOnSuccessListener {
                            val schools = it.toObjects(SchoolResponse::class.java)
                            val filteredSchools = schools.filter { school ->
                                school.schoolName?.lowercase()?.contains(schoolName.lowercase()) ?: false
                            }
                            _foundSchools.value = filteredSchools
                            _isLoading.value = false
                        }.addOnFailureListener {
                            _errorMessage.value = it.message
                            _isLoading.value = false
                        }
                }
            }

    fun updateUserDetailsSchools(school: SchoolResponse) {
        _isLoading.value = true
        val phoneNumber = firebaseAuth.currentUser?.phoneNumber
        if(!phoneNumber.isNullOrEmpty()) {
            viewModelScope.launch {
                _isLoading.value = false

                //get current schools array first, build a new array with school added, then update user
                firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).get().addOnSuccessListener {
                    val user = it.toObject(UserResponse::class.java)
                    val currentSchools = user?.schools ?: HashMap()

                    //create new hashmap with school added (school.id is the key) and value is false
                    val newSchools = HashMap<String, Boolean>()
                    newSchools[school.id.toString()] = false
                    currentSchools.putAll(newSchools)

                    //update user
                    firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).update("schools", currentSchools)
                        .addOnSuccessListener {
                            _schoolAdded.value = true
                            _isLoading.value = false
                        }.addOnFailureListener {
                            _schoolAdded.value = false
                            _errorMessage.value = it.message
                            _isLoading.value = false
                        }


                }.addOnFailureListener {
                    _errorMessage.value = it.message
                    _isLoading.value = false
                }
            }
        }
    }
}
