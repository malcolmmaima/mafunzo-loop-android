package com.mafunzo.loop.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mafunzo.loop.databinding.ActivitySplashBinding
import com.mafunzo.loop.ui.auth.AuthActivity
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.ui.main.MainActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSplash()
        initializeObservers()
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userExists.collectLatest { userExists ->
                    if (userExists) {
                        loadMainActivity()
                    } else {
                        loadAuth()
                    }
                }
            }
        }
    }

    private fun initSplash() {
        binding.progressBar.visible()
        lifecycleScope.launch {
            if(splashViewModel.isUserLoggedIn()){
                authViewModel.userPhoneNumber?.let { phoneNumber ->
                    authViewModel.fetchUser(phoneNumber)
                }
            } else {
                delay(3000)
                loadAuth()
            }
        }
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
}