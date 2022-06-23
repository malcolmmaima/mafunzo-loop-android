package com.mafunzo.loop.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mafunzo.loop.databinding.ActivitySplashBinding
import com.mafunzo.loop.ui.auth.AuthActivity
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.visible
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
        binding.progressBar.visible()

        //wait for 2 seconds then call loadMainActivity()
        Thread {
            Thread.sleep(2000)
            loadAuth()
        }.start()
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