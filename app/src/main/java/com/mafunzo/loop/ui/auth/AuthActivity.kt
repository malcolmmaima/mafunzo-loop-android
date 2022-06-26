package com.mafunzo.loop.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mafunzo.loop.databinding.ActivityAuthBinding
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val splashViewModel: SplashViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeObservers()
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userExists.collectLatest { exists ->
                    if (exists) {
                        loadMainActivity()
                    } else {
                        authViewModel.signOutFirebaseUser()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkLogInStatus()
    }

    private fun checkLogInStatus() {
        lifecycleScope.launch {
            delay(2000)
            if(splashViewModel.isUserLoggedIn()){
                //now check if user exists in db
                authViewModel.userPhoneNumber?.let { phoneNumber ->
                    authViewModel.fetchUser(phoneNumber)
                }
            }
        }
    }

    private fun loadMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}