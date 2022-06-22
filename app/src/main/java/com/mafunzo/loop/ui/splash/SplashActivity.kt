package com.mafunzo.loop.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel = SplashViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        initSplash()
    }

    private fun initSplash() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadAuth(){

    }

    private fun loadMainActivity(){

    }
}