package com.mafunzo.loop.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
    private val authViewModel: AuthViewModel by viewModels()
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
            authViewModel.userEnabled.collect { enabled ->
                userEnabled = enabled
                Log.d("SplashActivity", "User enabled: $userEnabled")
                if(enabled) {
                    splashViewModel.getSystemSettings()
                }

            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.systemSettings.collect { systemSetts ->
                    val allowedMaintainer = authViewModel.userPhoneNumber?.let {
                        systemSetts.maintainers?.contains(
                            it
                        ) ?: false
                    }
                    if(systemSetts.offline == true && allowedMaintainer == false) {
                        loadOffline()
                    } else {
                        lifecycleScope.launch {
                            authViewModel.userExists.observe(this@SplashActivity) { userExists ->
                                if (userExists) {
                                    Log.d("SplashActivity", "User exists")
                                    if (userEnabled) {
                                        Log.d("SplashActivity", "User exists and enabled")
                                        loadMainActivity()
                                    } else {
                                        Log.d("SplashActivity", "User exists but disabled")
                                        loadAccountDisabledActivity()
                                    }
                                } else {
                                    Log.d("SplashActivity", "User does not exist")
                                    loadAuth()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initSplash() {
        binding.progressBar.visible()
        lifecycleScope.launch {
            if(splashViewModel.isUserLoggedIn()){
                Log.d("SplashActivity", "User is logged in")
                authViewModel.userPhoneNumber?.let { phoneNumber ->
                    if(phoneNumber.isNotEmpty()){
                        authViewModel.fetchUser(phoneNumber)
                    } else {
                        loadAuth()
                    }
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