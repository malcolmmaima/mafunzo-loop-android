package com.mafunzo.loop.ui.auth.viewmodels

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.mafunzo.loop.data.local.database.MafunzoDatabase
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.requests.CreateUserRequest
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val firestoreDB: FirebaseFirestore,
    val localDB: MafunzoDatabase,
    val userPrefs: AppDatasource,
    val userRepository: UserRepository
) : ViewModel() {
    val TAG = "AuthViewModel"

    // we will use this to match the sent otp from firebase
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val _codeSent = MutableSharedFlow<Boolean>()
    val codeSent = _codeSent.asSharedFlow()

    private val _verificationId = MutableSharedFlow<String>()
    val verificationId = _verificationId.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _isLoading = MutableSharedFlow<Boolean>()
    val isLoading = _isLoading.asSharedFlow()

    private val _isOTPVerified = MutableSharedFlow<Boolean>()
    val isOTPVerified = _isOTPVerified.asSharedFlow()

    val userPhoneNumber = auth.currentUser?.phoneNumber

    private val _userCreated = MutableSharedFlow<Boolean>()
    val userCreated = _userCreated.asSharedFlow()

    private val _userExists = MutableSharedFlow<Boolean>()
    val userExists = _userExists.asSharedFlow()

    private val _userEnabled = MutableSharedFlow<Boolean>()
    val userEnabled = _userEnabled.asSharedFlow()

    private val _userDetails = MutableSharedFlow<UserResponse>()
    val userDetails = _userDetails.asSharedFlow()

    private val _loggedOut = MutableSharedFlow<Boolean>()
    val loggedOut = _loggedOut.asSharedFlow()

    fun initiateFirebaseCallbacks() {
        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    _isLoading.emit(false)
                    Log.d(TAG , "onVerificationCompleted Success")

                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.

                    signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                viewModelScope.launch {
                    _codeSent.emit(false)
                    _isLoading.emit(false)
                    _errorMessage.emit(e.message.toString())
                    Log.d(TAG , "onVerificationFailed Error: ${e.message}")
                }
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                viewModelScope.launch {
                    _codeSent.emit(false)
                    _isLoading.emit(false)
                    _errorMessage.emit("OTP Timeout")
                    Log.d(TAG , "onCodeAutoRetrievalTimeOut")
                }
            }

            // On code is sent by the firebase this method is called
            // in here we start OTP Fragment where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                viewModelScope.launch {
                    _verificationId.emit(verificationId)
                    _codeSent.emit(true)
                    _isLoading.emit(false)
                    resendToken = token
                    Log.d(TAG,"onCodeSent: $verificationId resendToken: $resendToken")
                }
            }
        }
    }

    fun sendVerificationCode(number: String, requireActivity: FragmentActivity) {
        viewModelScope.launch {
            _isLoading.emit(true)
            _codeSent.emit(false)

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity) // Activity to display the dialog on
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG , "Auth started")
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        _isOTPVerified.emit(true)
                        _isLoading.emit(false)
                        Log.d(TAG , "signInWithPhoneAuthCredential Success")
                    }
                } else {
                    viewModelScope.launch{
                        _isOTPVerified.emit(false)
                        _isLoading.emit(false)
                        _errorMessage.emit(task.exception?.message.toString())
                        Log.d(TAG , "signInWithPhoneAuthCredential Error: ${task.exception?.message}")
                    }
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        viewModelScope.launch {
                            _errorMessage.emit("Invalid code.")
                            _isLoading.emit(false)
                            Log.d(TAG , "signInWithPhoneAuthCredential Error: ${task.exception?.message}")
                        }
                    }
                }
            }
    }

    fun createUser(newUser: CreateUserRequest) {
        viewModelScope.launch {
            _isLoading.emit(true)
            val user = auth.currentUser
            if (user != null) {
                val phoneNumber = user.phoneNumber
                if(phoneNumber != null) {
                    firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).set(newUser).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                _isLoading.emit(false)
                                _userCreated.emit(true)
                                Log.d(TAG, "createUser Success")
                            }
                        } else {
                            viewModelScope.launch {
                                _isLoading.emit(false)
                                _userCreated.emit(false)
                                _errorMessage.emit(task.exception?.message.toString())
                                Log.d(TAG, "createUser Error: ${task.exception?.message}")
                            }
                        }
                    }
                }
            } else {
                _isLoading.emit(false)
                _userCreated.emit(false)
                _errorMessage.emit("User is null")
            }
        }
    }

    fun fetchUser(phoneNumber: String){
        Log.d(TAG , "fetchUser")
        if(!phoneNumber.isNullOrEmpty()){
            viewModelScope.launch {
                Log.d(TAG , "fetching user: $phoneNumber")
                userPrefs.clear()
                _isLoading.emit(true)
                firestoreDB.collection(Constants.FIREBASE_APP_USERS).document(phoneNumber).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.toObject(UserResponse::class.java)
                        if(user != null && user.accountType?.isNotEmpty() == true) {

                            viewModelScope.launch {
                                user.enabled?.let { _userEnabled.emit(it) }
                                _isLoading.emit(false)
                                _userExists.emit(true)
                                _userDetails.emit(user)
                                userRepository.insertUsertoRoom(user.toUserEntity(phoneNumber))
                                //save current workspace(school id) in shared pref
                                user.schools?.let {
                                    userPrefs.saveCurrentWorkspace(it.entries.first().key.trim(), it.entries.first().value)
                                    userPrefs.saveAccountType(user.accountType)
                                    Log.d(TAG, "Save current workspace: ${it.entries.first().key.trim()}")
                                }
                            }
                        } else {
                            viewModelScope.launch {
                                _isLoading.emit(false)
                                _userExists.emit(false)
                                Log.d(TAG, "fetchUser Error: User is null")
                            }
                        }
                    } else {
                        viewModelScope.launch {
                            _isLoading.emit(false)
                            _userExists.emit(false)
                            _errorMessage.emit(task.exception?.message.toString())
                            Log.d(TAG, "fetchUser Error: ${task.exception?.message}")
                        }
                    }
                }
            }
        }
    }

    suspend fun signOutUser() {
        auth.signOut()
        clearUserData()
        viewModelScope.launch {
            _loggedOut.emit(true)
        }
    }

    //clear room and shared prefs
    suspend fun clearUserData() {
        userRepository.clearUserData()
        userPrefs.clear()
    }
}