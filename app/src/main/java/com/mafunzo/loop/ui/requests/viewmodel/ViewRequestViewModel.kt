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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewRequestViewModel @Inject constructor(
    val firestoreDB: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,
    val userPrefs: AppDatasource
): ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _requestWithdrawn = MutableLiveData<Boolean>()
    val requestWithdrawn: LiveData<Boolean> = _requestWithdrawn


    fun withdrawRequest(request: UserRequestResponse?) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            viewModelScope.launch {
                _loading.value = true
                val currentWorkSpace = userPrefs.getCurrentWorkSpace().first()?.trim()
                val accountType = userPrefs.getAccountType().first()?.trim()

                if (currentWorkSpace != null && accountType != null) {
                    user.phoneNumber?.let {phonenumber ->
                        request?.let {withdrawRequest ->
                            if(withdrawRequest.id.isNotEmpty()){
                                //check if exists in firebase first
                                firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                                    .document(currentWorkSpace).collection(phonenumber).document(withdrawRequest.id).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            firestoreDB.collection(Constants.FIREBASE_REQUESTS)
                                                .document(currentWorkSpace).collection(phonenumber).document(withdrawRequest.id).delete()
                                                .addOnCompleteListener {
                                                    _loading.value = false
                                                    if (it.isSuccessful) {
                                                        _requestWithdrawn.value = true
                                                    }  else {
                                                        _requestWithdrawn.value = false
                                                        _errorMessage.value = it.exception?.message
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    _loading.value = false
                                                    _errorMessage.value = it.message
                                                }
                                        } else {
                                            _loading.value = false
                                            _errorMessage.value = "Request not found"
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        _loading.value = false
                                        exception.localizedMessage?.let { _errorMessage.value = exception.message }
                                    }
                            } else {
                                _loading.value = false
                                _errorMessage.value = "Request not found"
                            }
                        }
                    }
                }
            }
        }
    }
}