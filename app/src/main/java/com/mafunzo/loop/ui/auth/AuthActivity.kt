package com.mafunzo.loop.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mafunzo.loop.databinding.ActivityAuthBinding
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.splash.SplashViewModel
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLogInStatus()
    }

    override fun onResume() {
        super.onResume()
        checkLogInStatus()
    }

    private fun checkLogInStatus() {
        lifecycleScope.launch {
            delay(2000)
            if(splashViewModel.isUserLoggedIn()){
                loadMainActivity()
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