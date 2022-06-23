package com.mafunzo.loop.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mafunzo.loop.databinding.ActivitySplashBinding
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.gone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel = SplashViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSplash()
    }

    private fun initSplash() {
        binding.progressBar.gone()

        //wait for 2 seconds then call loadMainActivity()
        Thread {
            Thread.sleep(2000)
            loadMainActivity()
        }.start()
    }

    private fun loadAuth(){

    }

    private fun loadMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}