package com.mafunzo.loop.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.mafunzo.loop.databinding.ActivitySplashBinding
import com.mafunzo.loop.ui.auth.AccountDisabledActivity
import com.mafunzo.loop.ui.auth.AuthActivity
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.splash.viewmodel.SplashViewModel
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val splashViewModel: SplashViewModel by viewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSplash()
        initializeObservers()
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            splashViewModel.userEnabled.collect { enabled ->
                userEnabled = enabled
                Log.d("SplashActivity", "User enabled: $userEnabled")
                if(enabled) {
                    splashViewModel.getSystemSettings()
                } else {
                    loadAccountDisabledActivity()
                }

            }
        }

        splashViewModel.systemSettings.observe(this@SplashActivity) { settings ->
            val allowedMaintainer = splashViewModel.userPhoneNumber?.let {
                settings.maintainers?.contains(
                    it
                ) ?: false
            }
            Log.d("SplashActivity", "Allowed maintainer: $allowedMaintainer")
            if(settings.offline == true && allowedMaintainer == false) {
                Log.d("SplashActivity", "Offline mode enabled")
                loadOffline()
            } else {
                initializeOnlineObservers()
            }
        }
    }

    private fun initializeOnlineObservers() {
        splashViewModel.userExists.observe(this@SplashActivity) { userExists ->
            if (userExists) {
                Log.d("SplashActivity", "User exists")
                loadMainActivity()
            } else {
                Log.d("SplashActivity", "User does not exist")
                loadAuth()
            }
        }
    }


    private fun initSplash() {
        binding.progressBar.visible()
        lifecycleScope.launch {
            if(splashViewModel.isUserLoggedIn()){
                Log.d("SplashActivity", "User is logged in")
                val phoneNumber = auth.currentUser?.phoneNumber
                if (phoneNumber != null) {
                    splashViewModel.fetchUser(phoneNumber)
                } else {
                    loadAuth()
                }
            } else {
                Log.d("SplashActivity", "User is not logged in")
                delay(2000)
                loadAuth()
            }
        }
    }

    private fun loadOffline() {
        val intent = Intent(this, SystemOfflineActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun loadAuth(){
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun loadMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun loadAccountDisabledActivity() {
        val intent = Intent(this, AccountDisabledActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}