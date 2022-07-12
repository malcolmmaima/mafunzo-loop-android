package com.mafunzo.loop.ui.splash.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val auth: FirebaseAuth,
) : ViewModel() {

    fun isUserLoggedIn() = auth.currentUser != null
}